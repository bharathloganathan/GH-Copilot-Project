# postToolUse hook: when a tool edited a tracked source file, remind the agent to run the explicit sync workflow.
# Advisory only: Git diff plus `DocSyncCli analyze` remain the reliable change detection mechanism.
$ErrorActionPreference = 'SilentlyContinue'
$raw = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($raw)) { exit 0 }

$payload = $raw | ConvertFrom-Json
$toolName = if ($payload.toolName) { $payload.toolName } else { $payload.tool_name }
if ($toolName -notmatch '^(edit|create|write|str_replace|apply_patch|replace)') { exit 0 }

$args = if ($null -ne $payload.toolArgs) { $payload.toolArgs } else { $payload.tool_input }
$text = if ($args -is [string]) { $args } else { $args | ConvertTo-Json -Compress -Depth 5 }

if ($text -match '(frontend[\\/]|src[\\/]main[\\/]java[\\/])' -and $text -notmatch 'docsync') {
    Write-Host 'DocSync: tracked source edited. Before finishing, run DocSyncCli sync, then verify (/sync-docs).'
}
exit 0
