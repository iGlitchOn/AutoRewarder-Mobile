"""Fail if the phone UI does not mention the gradle versionName."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    gradle = (ROOT / "android" / "app" / "build.gradle").read_text(encoding="utf-8")
    name = re.search(r'versionName "([^"]+)"', gradle)
    code = re.search(r"versionCode (\d+)", gradle)
    if not name or not code:
        print("gradle version missing")
        return 1
    version = name.group(1)
    missing = []
    for rel in ("gui/phone.html", "gui/phone.js"):
        if version not in (ROOT / rel).read_text(encoding="utf-8"):
            missing.append(rel)
    if missing:
        print("missing", version, ",".join(missing))
        return 1
    print("version ok", version, code.group(1))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
