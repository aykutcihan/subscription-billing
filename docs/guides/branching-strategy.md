# Branching strategy

## Branch model

```
master          ← stable / release-ready
  └── develop   ← integration branch (all features land here first)
        └── feature/...   ← one branch per task
        └── fix/...       ← one branch per bug fix
        └── refactor/...  ← one branch per refactor
```

## Workflow

1. **Branch off `develop`**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-feature
   ```

2. **Work on the feature branch.** Commit often with descriptive messages following
   [Conventional Commits](https://www.conventionalcommits.org/):
   `feat(auth): ...`, `fix(ci): ...`, `refactor(auth): ...`, `docs: ...`

3. **Open a PR targeting `develop`** (not `master`).
   ```bash
   git push -u origin feature/my-feature
   gh pr create --base develop
   ```

4. **CI must pass** before merge. See `.github/workflows/ci.yml`.

5. **Merge into `master`** only for releases (via PR from `develop`).

## Rules

| Branch | Push directly? | PR required? | CI gate? |
|---|---|---|---|
| `master` | No | Yes (from `develop`) | Yes |
| `develop` | No | Yes (from feature branch) | Yes |
| `feature/*`, `fix/*`, `refactor/*` | Yes | — | Triggered on push |

## Branch naming

| Prefix | Use for |
|---|---|
| `feature/` | New functionality |
| `fix/` | Bug fixes |
| `refactor/` | Code restructuring without behaviour change |
| `docs/` | Documentation-only changes |
| `chore/` | Build, CI, dependency updates |
