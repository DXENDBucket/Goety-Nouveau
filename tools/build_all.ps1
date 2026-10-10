param(
    [string]$ConfluxCheckout,
    [string]$NeoJavaHome = $env:JAVA_HOME,
    [string]$ForgeJavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
function Invoke-TargetBuild([string]$directory, [string]$java, [string]$core, [string[]]$tasks) {
    $previousJava = $env:JAVA_HOME
    Push-Location -LiteralPath $directory
    try {
        if ($java) { $env:JAVA_HOME = $java }
        $arguments = @($tasks) + '--console=plain'
        if ($core) { $arguments += "-PconfluxCheckout=$core" }
        # Windows PowerShell treats javac's stderr warnings as ErrorRecords.
        # Judge native builds by their exit code rather than the presence of warnings.
        $previousErrorAction = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            & .\gradlew.bat @arguments 2>&1 | ForEach-Object { $_.ToString() }
            $buildExit = $LASTEXITCODE
        } finally { $ErrorActionPreference = $previousErrorAction }
        if ($buildExit -ne 0) { throw "Build failed in $directory (exit $buildExit)" }
    } finally {
        $env:JAVA_HOME = $previousJava
        Pop-Location
    }
}

$neoCore = if ($ConfluxCheckout) { (Resolve-Path -LiteralPath $ConfluxCheckout).Path } else { $null }
$forgeCore = if ($neoCore) { Join-Path $neoCore 'forge' } else { $null }
Invoke-TargetBuild $taskRoot $NeoJavaHome $neoCore @(':jar', ':sourcesJar', ':ars-conflux:jar')
Invoke-TargetBuild (Join-Path $taskRoot 'forge') $ForgeJavaHome $forgeCore @(':jar', ':sourcesJar', ':ars-conflux:jarJar')
