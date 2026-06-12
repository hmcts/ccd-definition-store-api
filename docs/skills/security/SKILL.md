---
name: ccd-definition-store-security
description: Use when working in the HMCTS `ccd-definition-store-api` repository on general Spring Security configuration, IDAM/OIDC integration, security-related regression testing, or other security changes that are not specifically about JWT issuer validation. For JWT issuer validation, use `docs/skills/security-jwt-issuer/SKILL.md`.
---

# Security

## Overview

Use this skill for general security changes in `ccd-definition-store-api`.
For JWT issuer validation and pipeline issuer-verification work, use [`docs/skills/security-jwt-issuer/SKILL.md`](../security-jwt-issuer/SKILL.md).

## Workflow

1. Check current state with `git status --short` and inspect local diffs before editing.
2. Review the relevant security configuration, runtime wiring, and tests for the change you are making.
3. Search for Spring Security, IDAM/OIDC, authentication, and authorization classes before changing behavior.
4. If the task turns into JWT issuer-validation work, switch to [`docs/skills/security-jwt-issuer/SKILL.md`](../security-jwt-issuer/SKILL.md).

## References

- JWT issuer-specific guidance: [`docs/skills/security-jwt-issuer/SKILL.md`](../security-jwt-issuer/SKILL.md)
