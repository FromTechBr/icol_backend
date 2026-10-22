#!/usr/bin/env python3
"""Carrega as variáveis de .env e inicia o backend Spring Boot."""

import os
import subprocess
import sys
from pathlib import Path


def load_env_file(env_path: Path) -> bool:
    if not env_path.is_file():
        print("Arquivo .env não encontrado!")
        return False

    with env_path.open(encoding="utf-8") as env_file:
        for line_number, raw_line in enumerate(env_file, start=1):
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue

            if line.startswith("export "):
                line = line[len("export ") :].lstrip()

            key, separator, value = line.partition("=")
            key = key.strip()
            if not separator or not key:
                print(f"Formato inválido no .env, linha {line_number}.", file=sys.stderr)
                return False

            value = value.strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
                value = value[1:-1]
            elif " #" in value:
                value = value.split(" #", maxsplit=1)[0].rstrip()

            os.environ[key] = value

    return True


def main() -> int:
    backend_dir = Path(__file__).resolve().parent
    if not load_env_file(backend_dir / ".env"):
        return 1

    print("Variáveis carregadas:")
    print(
        "   DB_URL: "
        + os.environ.get("DB_URL", "jdbc:postgresql://localhost:5432/icol_db")
    )
    print("   DB_USERNAME: " + os.environ.get("DB_USERNAME", "postgres"))
    print("   JWT_SECRET: configurado (valor ocultado)")
    print()
    print("Iniciando aplicação...")

    app_dir = backend_dir
    try:
        return subprocess.run(
            ["mvn", "spring-boot:run"],
            cwd=app_dir,
            env=os.environ.copy(),
            check=False,
        ).returncode
    except FileNotFoundError:
        print("Maven não encontrado. Instale Maven ou adicione-o ao PATH.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
