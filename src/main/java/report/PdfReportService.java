package report;

//bibliotecas necesarias para generar el reporte como pdf.
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPageEventHelper;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

//fuentes y colores parte del reporte
public class PdfReportService {
    private static final Font FUENTE_SISTEMA = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
    private static final Font FUENTE_METADATA = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font FUENTE_ENCABEZADO_TABLA = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_CELDA = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Color COLOR_ENCABEZADO_TABLA = new Color(180, 0, 0);
    private static final Color COLOR_CELDA_OCUPADA = new Color(255, 245, 180);

    //tabla
    public void generarReporteTabla(String rutaSalida, ReportHeader header, ReportTable tabla)
            throws ReportException {
        generarReporte(rutaSalida, header, tabla, null);
    }

    //grafico del reporte
    public void reporteGrafico(String rutaSalida, ReportHeader header, ReportTable tabla, byte[] imagenGraficoPng)
            throws ReportException {
        generarReporte(rutaSalida, header, tabla, imagenGraficoPng);
    }

    //generar el reporte y sus respectivas validaciones
    private void generarReporte(String rutaSalida, ReportHeader header, ReportTable tabla, byte[] imagenGraficoPng)
            throws ReportException {
        if (rutaSalida == null || rutaSalida.isBlank()) {
            throw new ReportException("La ruta de salida del reporte no puede estar vacía.");
        }
        if (header == null) {
            throw new ReportException("El reporte requiere un encabezado.");
        }
        if (tabla == null) {
            throw new ReportException("El reporte requiere una tabla con datos.");
        }

        Document documento = new Document(PageSize.A4, 36, 36, 54, 54);
        try (FileOutputStream salida = new FileOutputStream(rutaSalida)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, header);
            agregarTabla(documento, tabla);

            if (imagenGraficoPng != null && imagenGraficoPng.length > 0) {
                agregarGrafico(documento, imagenGraficoPng);
            }

            documento.close();

        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new ReportException("No fue posible generar el reporte PDF: " + e.getMessage(), e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    //encabezado
    private void agregarEncabezado(Document documento, ReportHeader header) throws DocumentException {
        Paragraph nombreSistema = new Paragraph(header.getNombreSistema(), FUENTE_SISTEMA);
        nombreSistema.setAlignment(Element.ALIGN_LEFT);
        documento.add(nombreSistema);

        Paragraph titulo = new Paragraph(header.getTitulo(), FUENTE_TITULO);
        titulo.setSpacingBefore(4f);
        titulo.setSpacingAfter(8f);
        documento.add(titulo);

        Paragraph metadata = new Paragraph();
        metadata.setFont(FUENTE_METADATA);
        metadata.add("Generado: " + header.getFechaGeneracionFormateada() + "\n");
        metadata.add("Usuario: " + header.getUsuarioGenera() + "\n");
        if (header.tieneFiltros()) {
            metadata.add("Filtros aplicados: " + header.getFiltrosAplicados() + "\n");
        }
        metadata.setSpacingAfter(14f);
        documento.add(metadata);
    }

    //tabla
    private void agregarTabla(Document documento, ReportTable tabla) throws DocumentException {
        if (tabla.estaVacia()) {
            Paragraph sinDatos = new Paragraph(
                    "No se encontraron datos para los criterios seleccionados.",
                    new Font(Font.HELVETICA, 11, Font.ITALIC));
            documento.add(sinDatos);
            return;
        }

        int columnas = tabla.cantidadColumnas();
        PdfPTable tablaPdf = new PdfPTable(columnas);
        tablaPdf.setWidthPercentage(100);
        tablaPdf.setHeaderRows(1); // repite el encabezado si la tabla pasa de página

        for (String encabezado : tabla.getEncabezados()) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO_TABLA));
            celda.setBackgroundColor(COLOR_ENCABEZADO_TABLA);
            celda.setPadding(6f);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaPdf.addCell(celda);
        }

        for (List<String> fila : tabla.getFilas()) {
            for (String valor : fila) {
                PdfPCell celda = new PdfPCell(new Phrase(valor, FUENTE_CELDA));
                celda.setPadding(5f);
                // Si la celda representa una ocupación (matrices de calendarización
                // o programación), se resalta cuando no está vacía y no es la
                // primera columna (etiqueta de hora).
                tablaPdf.addCell(celda);
            }
        }

        documento.add(tablaPdf);
    }


    //generar el reporte en forma de matriz,
    //esta parte es fundamental para la visualizacion de los reportes

    public void generarReporteMatriz(String rutaSalida, ReportHeader header, ReportTable matriz)
            throws ReportException{
        if(rutaSalida == null || rutaSalida.isBlank()){
            throw new ReportException("La ruta de salida del reporte no puede estar vacía..");
        }
        if(header == null || matriz == null){
            throw new ReportException("El reporte requiere un encabezado y una matriz con datos..");
        }

        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 54, 54);

        try(FileOutputStream salida = new FileOutputStream(rutaSalida)) {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePaginaConNumero());
            documento.open();

            agregarEncabezado(documento, header);

            if(matriz.estaVacia()){
               documento.add(new Paragraph("No se encontraron datos para los criterios seleccionados.",
                        new Font(Font.HELVETICA, 11, Font.ITALIC)));
               documento.close();
               return;
            }

            int columnas = matriz.cantidadColumnas();
            PdfPTable tablaPdf = new PdfPTable(columnas);
            tablaPdf.setWidthPercentage(100);
            tablaPdf.setHeaderRows(1);

            for(String encabezado : matriz.getEncabezados()){
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO_TABLA));
                celda.setBackgroundColor(COLOR_ENCABEZADO_TABLA);
                celda.setPadding(5f);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaPdf.addCell(celda);
            }

            for(List<String> fila : matriz.getFilas()){
                for(int col = 0; col < fila.size(); col++){
                    String valor = fila.get(col);
                    String valorSeguro = valor != null ? valor : "";
                    PdfPCell celda = new PdfPCell(new Phrase(valorSeguro, FUENTE_CELDA));
                    celda.setPadding(4f);

                    boolean esColumnaEtiqueta = (col == 0);
                    boolean ocupada = !esColumnaEtiqueta && !valorSeguro.isBlank();

                    if (ocupada) {
                        celda.setBackgroundColor(COLOR_CELDA_OCUPADA);
                    }
                    tablaPdf.addCell(celda);
                }
            }
            documento.add(tablaPdf);
            documento.close();
        }catch (Exception e){
            throw new ReportException("No fue posible generar el reporte de Matriz: " + e.getMessage(), e);
        }finally{
            if(documento.isOpen()) {
                documento.close();
            }
        }
    }

    //metodo para agregar el grafico al reporte
    private void agregarGrafico(Document documento, byte[] imagenPng) throws Exception {
        try {

            Image imagen = Image.getInstance(imagenPng);
            imagen.scaleToFit(480f, 320f);
            imagen.setAlignment(Element.ALIGN_CENTER);
            imagen.setSpacingBefore(14f);
            documento.add(imagen);
        } catch (Exception e) {
            Paragraph aviso = new Paragraph("No se pudo incluir el gráfico en el reporte.",
                    new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY));
            try {
                documento.add(aviso);
            } catch (DocumentException ignored) {

            }
        }
    }

    private static class PiePaginaConNumero extends PdfPageEventHelper {

        private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte contenido = writer.getDirectContent();
            Phrase pie = new Phrase("Página " + writer.getPageNumber(), FUENTE_PIE);
            ColumnText.showTextAligned(
                    contenido,
                    Element.ALIGN_CENTER,
                    pie,
                    (documento.right() + documento.left()) / 2,
                    documento.bottom() - 20,
                    0);
        }
    }

}
