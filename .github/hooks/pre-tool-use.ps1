# preToolUse hook: denies commit/push/merge/deploy and destructive commands. Payload: { toolName, toolArgs } on stdin.
$ErrorActionPreference = 'Stop'
try {
    $raw = [Console]::In.ReadToEnd()
    if ([string]::IsNullOrWhiteSpace($raw)) { exit 0 }
    $payload = $raw | ConvertFrom-Json

    $toolName = if ($payload.toolName) { $payload.toolName } else { $payload.tool_name }
    $args = if ($null -ne $payload.toolArgs) { $payload.toolArgs } elseif ($null -ne $payload.tool_input) { $payload.tool_input } else { '' }
    $text = if ($args -is [string]) { $args } else { $args | ConvertTo-Json -Compress -Depth 5 }

    $isShell = $toolName -match '^(bash|powershell|shell|execute|run_in_terminal)'
    $deny = $null

    if ($isShell) {
        $rules = @(
            @{ Pattern = '\bgit\s+(commit|push|merge|tag|rebase)\b'; Reason = 'Commit/push/merge needs explicit human authorization. Ask the user to run it.' },
            @{ Pattern = '\bgit\s+(reset\s+--hard|clean\b|checkout\s+--\s)'; Reason = 'Destructive Git operation; ask the user first.' },
            @{ Pattern = '\bgh\s+(pr\s+merge|repo\s+delete|release\s+create)'; Reason = 'Merge/delete/release requires human approval.' },
            @{ Pattern = '(?i)\b(rm\s+-rf|rm\s+-fr|rmdir\s+/s|del\s+/s|Remove-Item\b[^|]*-Recurse)'; Reason = 'Recursive delete is destructive; ask the user first.' },
            @{ Pattern = '(?i)\b(mvn|mvnw)(\.cmd)?\s+[^|]*\b(deploy|release:perform)\b'; Reason = 'Deploy requires human approval.' }
        )
        foreach ($rule in $rules) {
            if ($text -match $rule.Pattern) { $deny = $rule.Reason; break }
        }
    }

    if ($deny) {
        @{ permissionDecision = 'deny'; permissionDecisionReason = "DocSync safety: $deny" } | ConvertTo-Json -Compress
    }
} catch {
    # Fail open: a broken hook must not block the session, but surface the problem on stderr.
    [Console]::Error.WriteLine("pre-tool-use hook error: $($_.Exception.Message)")
}
exit 0
