# sessionStart hook: reminds the agent of the sync workflow and flags source files newer than the last report.
$ErrorActionPreference = 'SilentlyContinue'
$null = [Console]::In.ReadToEnd()

$root = (Get-Location).Path
$report = Join-Path $root 'docs/reports/sync-report.md'
$sources = @(Get-ChildItem -Path (Join-Path $root 'frontend'), (Join-Path $root 'src/main/java') -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -notmatch '[\\/]docsync[\\/]' })

if (-not (Test-Path $report)) {
    Write-Host 'DocSync: no sync report yet. Run: DocSyncCli sync --root .'
} else {
    $stamp = (Get-Item $report).LastWriteTimeUtc
    $newer = @($sources | Where-Object { $_.LastWriteTimeUtc -gt $stamp })
    if ($newer.Count -gt 0) {
        Write-Host "DocSync: $($newer.Count) source file(s) changed since the last sync report. Run /sync-docs."
    } else {
        Write-Host 'DocSync: documentation report is newer than all sources.'
    }
}
exit 0
