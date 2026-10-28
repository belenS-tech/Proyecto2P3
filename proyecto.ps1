param(
    [ValidateSet('iniciar','cliente','servidor','verificar','pruebas','instalar','compilar')][string]$Accion = 'iniciar',
    [string]$Servidor = 'localhost',
    [ValidateRange(1,65535)][int]$Puerto = 5050
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    # Preferir Java 21, aunque Windows tenga Java 8 en PATH.
    $proyectoJdk = Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '21[.]' } | Select-Object -First 1
    if ($proyectoJdk) { $env:JAVA_HOME = $proyectoJdk.FullName }
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
        throw 'Configure JAVA_HOME con un JDK 21 antes de ejecutar.'
    }
    $proyectoMaven = Get-ChildItem 'C:\Program Files\JetBrains' -Directory -Filter 'IntelliJ IDEA*' -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'plugins/maven/lib/maven3/bin/mvn.cmd' } |
        Where-Object { Test-Path $_ } | Select-Object -First 1
    if (-not $proyectoMaven) { $proyectoMaven = 'mvn.cmd' }
    switch ($Accion) {
        'iniciar' { & $proyectoMaven compile exec:java "-Dserver.host=$Servidor" "-Dserver.port=$Puerto" }
        'cliente' { & $proyectoMaven compile exec:java "-Dserver.host=$Servidor" "-Dserver.port=$Puerto" }
        'servidor' { & $proyectoMaven compile exec:java '-Dexec.mainClass=server.BackendMain' "-Dserver.port=$Puerto" }
        'compilar' { & $proyectoMaven package '-DskipTests' }
        'verificar' { & $proyectoMaven compile exec:java '-Dexec.mainClass=database.OracleSetup' '-Dexec.args=check' }
        'instalar' { & $proyectoMaven compile exec:java '-Dexec.mainClass=database.OracleSetup' '-Dexec.args=install' }
        'pruebas' { & $proyectoMaven verify '-Doracle.it=true' }
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven termino con codigo $LASTEXITCODE." }
} finally { Pop-Location }
