"""Genera una importación Oracle de los XML originales sin modificarlos."""
from pathlib import Path
import xml.etree.ElementTree as ET
from datetime import time

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data"

def literal(value):
    return "'" + value.replace("'", "''") + "'"

def exportar():
    lines = ["-- Importación inicial: ejecutar una sola vez, después de 01_schema.sql.",
             "-- Generado desde src/main/resources/data; los XML se conservan.",
             "WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK", "SET DEFINE OFF"]
    report = ["Cambios de ID para conservar reservas duplicadas:"]
    counts = {}
    def insert(table, fields, values):
        lines.append(f"INSERT INTO p3_{table} ({','.join(fields)}) VALUES ({','.join(values)});")
        counts[table] = counts.get(table, 0) + 1

    for table, tag, fields in [
        ("usuarios", "usuario", ["id", "clave", "rol"]),
        ("funcionarios", "funcionario", ["id", "nombre", "telefono"]),
        ("categorias", "categoria", ["id", "descripcion"]),
    ]:
        for row in ET.parse(DATA / (table + ".xml")).getroot().findall(tag):
            insert(table, fields, [literal(row.findtext(f)) for f in fields])
    for row in ET.parse(DATA / "recursos.xml").getroot().findall("recurso"):
        insert("recursos", ["id", "categoria_id", "descripcion"],
               [literal(row.findtext(f)) for f in ["id", "categoria", "descripcion"]])

    rows = ET.parse(DATA / "reservas.xml").getroot().findall("reserva")
    reserved = {r.findtext("id") for r in rows}
    used = set()
    for index, row in enumerate(rows, 1):
        old = row.findtext("id")
        key = old
        suffix = index
        while key in used:
            key = f"RES-MIG-{suffix:06}"
            suffix += 1
            if key in reserved:
                key = old
        used.add(key)
        if key != old:
            report.append(f"Reserva #{index}: {old} -> {key} ({row.findtext('actividad')}, {row.findtext('fecha')})")
        insert("reservas", ["id", "actividad", "fecha", "hora_inicio", "hora_fin", "funcionario_id", "estado"],
               [literal(key), literal(row.findtext("actividad")),
                "TO_DATE(" + literal(row.findtext("fecha")) + ",'YYYY-MM-DD')",
                literal(time.fromisoformat(row.findtext("horaInicio")).strftime("%H:%M:%S")),
                literal(time.fromisoformat(row.findtext("horaFin")).strftime("%H:%M:%S")),
                literal(row.findtext("funcionarioId")), literal(row.findtext("estado"))])
        for detail in row.findall("detalles/detalle"):
            insert("detalles", ["reserva_id", "recurso_id"], [literal(key), literal(detail.findtext("recursoId"))])
    lines.append("COMMIT;")
    (ROOT / "database/02_datos_xml.sql").write_text("\n".join(lines) + "\n", encoding="utf-8")
    report.extend(["", "Registros importados:"] + [f"{k}: {v}" for k, v in counts.items()])
    (ROOT / "database/migracion_ids.txt").write_text("\n".join(report) + "\n", encoding="utf-8")
    print("\n".join(report))

if __name__ == "__main__":
    exportar()
