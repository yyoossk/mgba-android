#!/bin/bash

# Check latest build status and analyze errors
REPO="yyoossk/mgba-android"

echo "=== Latest Build Status ==="
LATEST_RUN=$(gh run list --repo $REPO --limit 1 --json databaseId -q '.[0].databaseId')
echo "Run ID: $LATEST_RUN"

# Get all jobs and steps
echo ""
echo "=== Failed Steps ==="
gh api repos/$REPO/actions/runs/$LATEST_RUN/jobs -q '.jobs[].steps[] | select(.conclusion=="failure") | {name: .name, status: .status}' 2>/dev/null || echo "Could not fetch job details"

# Get run status
echo ""
echo "=== Run Status ==="
gh api repos/$REPO/actions/runs/$LATEST_RUN -q '{conclusion: .conclusion, status: .status}' 2>/dev/null || echo "Could not fetch run status"
