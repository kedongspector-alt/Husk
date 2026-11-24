# Push to your fork and build on GitHub Actions

## One-time setup

1. **Install Git for Windows** — https://git-scm.com/download/win (or `winget install Git.Git`)
2. **Install GitHub CLI** — https://cli.github.com/ (or `winget install GitHub.cli`)
3. **Sign in**:
   ```powershell
   gh auth login
   # pick: GitHub.com, HTTPS, Yes (authenticate Git), Login with a web browser
   ```
4. Restart PowerShell so `git` and `gh` are on PATH.

## Push this folder as your own fork

Run from `C:\Users\maxey\Documents\xcl\Husk-main`:

```powershell
git init -b main
git add -A
git -c user.email="maxeyspector1@gmail.com" -c user.name="maxey" commit -m "Husk + Play Integrity stub"
gh repo create Husk --public --source=. --push
```

`gh repo create ... --push` creates `maxey/Husk` on GitHub.com and pushes to it. The workflow at `.github/workflows/build.yml` runs automatically on push.

## Watch the build

```powershell
gh run watch
```

Or open the Actions tab at `https://github.com/<your-user>/Husk/actions`.

First build takes **2-4 hours** (ANGLE + MoltenVK + QEMU + Rust pairing all compile from source). Subsequent builds that only touch Java/Swift are much faster because the sysroot is cached.

## Download the IPA

Once green:

```powershell
gh run download --name Husk-ipa
```

Produces `Husk.ipa` and `Husk.tipa` in the current directory. Sideload `Husk.ipa` via SideStore/AltStore, or `Husk.tipa` via TrollStore.

## Rebuild after edits

Edit your files, then:

```powershell
git add -A
git commit -m "describe what changed"
git push
```

The workflow re-runs automatically.

## Caveats

- **macOS runner minutes cost ~10x Linux minutes.** On a free account you have 2,000 Linux-minutes/mo, which equals 200 macOS-minutes. A single full Husk build will blow that. You'll either need a paid plan, a self-hosted macOS runner, or a real Mac.
- **This fork is public.** If you want it private, change `--public` to `--private` in the `gh repo create` command above. Private repos get even fewer macOS minutes on free accounts.
- **GPL-2.0.** Husk is GPL-2.0-or-later. Publishing a fork is fine. Distributing a modified build also fine, but it must carry the same license and the source must be available.
- **The `.gitignore` already in the repo excludes `build/`, so pushing won't try to ship a stale build.**

## If something fails on the runner

- Open the failed run, download the `build-logs` artifact.
- Common failures: (a) new Homebrew package versions break meson, (b) Xcode version mismatch, (c) ANGLE or MoltenVK upstream moves.
- For edits to just the Java framework stubs: the sysroot cache means reruns skip the heavy parts, so iteration is cheap once the first build succeeds.
