# Nützliche Links
- RISCV-Manual: [riscv-unprivileged](https://docs.riscv.org/reference/isa/_attachments/riscv-unprivileged.pdf)
    - 43: 32-Bit Instructions
    - 62: 64-Bit Instructions
    - 629: Übersicht
(Seitenangaben im PDF)

- RISCV-Instruction encoder und decoder [https://luplab.gitlab.io/rvcodecjs/](https://luplab.gitlab.io/rvcodecjs/) 
- ELF-Format: [Wikipedia](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format)
- Ecall tabelle: 





# VM

## Windows

- `cloudflared` executable runterladen
- Absoluten Pfad von Cloudflare executable kopieren
- ggf Ordner `.ssh` erstellen (in eurem Benutzerorder)
- `.ssh/config` (Datei) editieren (texteditor):
```
Host bwinf.francloud.org
ProxyCommand ABSOLUTER_PFAD/cloudflared.exe access ssh --hostname %h
```
- `ssh bwinf@bwinf.francloud.org`

## Linux
- `cloudflared` und `ssh` installieren
- `.ssh/config` editieren:
```
Host bwinf.francloud.org
ProxyCommand /usr/local/bin/cloudflared access ssh --hostname %h
```
- `ssh bwinf@bwinf.francloud.org`

## Dateien hoch/runterladen
` scp <source> destination`
Dateien auf dem Server: `bwinf@bwinf.francloud.org:/home/bwinf/test.txt`

# Beachten
- Unterschied der Shift instructions zwischen 32 und 64 bit
- Stack pointer!!!

## Doom 
- Wenn komische writes passieren die nirgendwo im RAM sind, dann ggf UART

