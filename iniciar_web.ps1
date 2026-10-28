$ErrorActionPreference = 'Stop'

$projectRoot = $PSScriptRoot
$pom = Join-Path $projectRoot 'web\pom.xml'
$war = Join-Path $projectRoot 'web\target\angur-web.war'

$maven = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $maven) {
    throw 'No se encontro Maven. Instala Maven y agregalo al PATH antes de iniciar la web.'
}

Write-Host 'Compilando la API y la interfaz web...'
& $maven.Source -f $pom package
if ($LASTEXITCODE -ne 0) {
    throw 'Maven no pudo compilar la aplicacion web.'
}
if (-not (Test-Path -LiteralPath $war)) {
    throw "No se genero el WAR esperado en $war."
}

$tomcatHome = $env:CATALINA_HOME
if (-not $tomcatHome) {
    $tomcatCandidates = @(
        (Join-Path $env:ProgramFiles 'Apache Software Foundation\apache-tomcat-10.1.54'),
        (Join-Path $env:ProgramFiles 'Apache Software Foundation\Tomcat 10.1')
    )
    $tomcatHome = $tomcatCandidates | Where-Object {
        Test-Path -LiteralPath (Join-Path $_ 'bin\catalina.bat')
    } | Select-Object -First 1
}
if (-not $tomcatHome -or -not (Test-Path -LiteralPath (Join-Path $tomcatHome 'bin\catalina.bat'))) {
    throw 'No se encontro Tomcat 10.1. Define CATALINA_HOME con la carpeta de Tomcat.'
}

if (-not $env:JAVA_HOME) {
    $jdkCandidates = @(
        (Join-Path $env:ProgramFiles 'Java\jdk-26'),
        (Join-Path $env:ProgramFiles 'Java\jdk-17')
    )
    $env:JAVA_HOME = $jdkCandidates | Where-Object {
        Test-Path -LiteralPath (Join-Path $_ 'bin\java.exe')
    } | Select-Object -First 1
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    throw 'Define JAVA_HOME con la carpeta de un JDK 17 o posterior.'
}

$tomcatBase = Join-Path $projectRoot '.qa_runtime\tomcat'
New-Item -ItemType Directory -Path $tomcatBase -Force | Out-Null
$tomcatConf = Join-Path $tomcatBase 'conf'
if (-not (Test-Path -LiteralPath (Join-Path $tomcatConf 'server.xml'))) {
    Copy-Item -LiteralPath (Join-Path $tomcatHome 'conf') -Destination $tomcatConf -Recurse
}
foreach ($directory in @('logs', 'temp', 'webapps', 'work')) {
    New-Item -ItemType Directory -Path (Join-Path $tomcatBase $directory) -Force | Out-Null
}
Copy-Item -LiteralPath $war -Destination (Join-Path $tomcatBase 'webapps\angur-web.war') -Force

$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $tomcatBase
if (-not $env:ANGUR_DB_URL) {
    $env:ANGUR_DB_URL = 'jdbc:mysql://127.0.0.1:3306/angur_gestion?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC'
}
if (-not $env:ANGUR_DB_USER) {
    $dbUser = Read-Host 'Usuario MySQL de la aplicacion [angur_app]'
    $env:ANGUR_DB_USER = if ($dbUser) { $dbUser } else { 'angur_app' }
}
if (-not $env:ANGUR_DB_PASSWORD) {
    $securePassword = Read-Host "Contrasena MySQL de $env:ANGUR_DB_USER" -AsSecureString
    $env:ANGUR_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
}

Write-Host 'Tomcat esta iniciando. Deja esta ventana abierta.'
Write-Host 'Abre http://localhost:8080/angur-web/ en el navegador.'
& (Join-Path $tomcatHome 'bin\catalina.bat') run
