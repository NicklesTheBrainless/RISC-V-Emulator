#import "@preview/polylux:0.4.0": *
#import "@preview/rivet:0.3.0" : schema
#import "@preview/sns-polylux-template:0.2.0": *
#import "@preview/commute:0.3.0": node, arr, commutative-diagram

/*
  ACHTUNG: You should seriously consider downloading
  the recommended fonts for this package.
  Make sure to install the static versions, because
  Typst does not support variable fonts yet.
  You can find the fonts here:
    https://fonts.google.com/share?selection.family=Roboto+Condensed
    https://fonts.google.com/share?selection.family=Raleway
  You can find the installation guide here:
    https://typst.app/docs/reference/text/text/#parameters-font
*/

#set text(lang: "en")
#show: sns-polylux-template.with(
  aspect-ratio    : "16-9",
  title           : [RISC-V CPU Emulator],
  //subtitle        : [Subtitle],
  event           : [Jugendforum Informatik],
  short-title     : [RISCV],
  short-event     : [Jugendforum Informatik],
  authors         : (
    {
      set text(top-edge: 0pt, bottom-edge: 0pt)
      grid(gutter: 2em, columns: (1fr, 1.8fr),
        align(right,[Christian Krause]),
        align(left,[#link("christian.krause@stud.uni-heidelberg.de")])
      )
    },{
    }
  )
)

#show raw: set text(font: "JetBrains Mono")
#title-slide()
#show raw.where(block: true): set text(0.8em)
#show raw.where(block: true): set par(leading: 0.2em)


#toc-slide( title: [Table of Contents] )

#slide(
  title: [Übersicht],
//  subtitle: [Slide subtitle],
)[
  === Was passiert, bevor ein Programm ausgeführt wird?
  + C-Programm (`hello_world.c`) 
  + Assembly Datei (`hello_world.s`) 
  + Ausführbare Datei (ELF) (`hello_world`)
]
#show raw: it => block(
  fill: rgb("#1d2433"),
  inset: 10pt,
  radius: 5pt,
  text(fill: rgb("#a2aabc"), it)
)
#slide(
  title : [hello_world.c]
)[

```c
#include <stdio.h>
int main() {
	int a = 3;
	int b = 37;
	int c = a + b;
	c += 2;
 	printf("Hello World! %d\n",c);
	return 0;
}
```
  #uncover(2)[
    ```sh
    $ gcc -S hello_world.c
    $ /opt/riscv/bin/riscv64-unknown-elf-gcc -S hello_world.c -O0 -mabi=lp64 -march=rv64i
    ```
  ]

]

#slide(
  title: [`hello_world.s`]
)[

  #columns(2)[
  ```yasm
main:
... 
	li	a4,3
	li	a5,37
	addw	a5,a4,a5
	addiw	a5,a5,2
...
	call	printf
...

  ```

    #colbreak()
```c
#include <stdio.h>
int main() {
	int a = 3;
	int b = 37;
	int c = a + b;
	c += 2;
	printf("Hello World! %d\n",c);
	return 0;
}
```
  ]
]
#slide(
  title: [Assembly Grundlagen]
)[
 - CPU: 
  - Register ($32 times 64$bit) (+ ggf. float Register)
  - Instructions 
  - Program Counter (PC) (Position im Programm)
 -  RAM
]

#slide(
  title: [Beispiele für Instructions]
)[
  - ```yasm li $rd $imm``` (Load immediate): Läd _imm_ in _rd_
  - ```yasm addi $rd $rs1 $imm``` (Add immediate): Addiert _imm_ auf _rs1_ und schreibt das Ergebnis in _rd_ 
  - ```yasm sub $rd $rs1 $rs2 ``` Subtrahiert _rs2_ von _rs1_ und speichert das Ergebnis in _rd_
]

#slide(
  title: [Speicher]
)[
  - ```yasm ld $rd $offset($rsi)``` (Load Doubleword): Läd den 64-bit Wert an _rs1_ + _offset_ aus dem Arbeitsspeicher in _rd_
  - ```yasm sd $rs2 $offset($rs1)``` (Store Douleword): Speichert den 64-bit Wert aus _rs2_ an die Stelle _offset_ + _rs1_
]





#slide(
  title: [Speicher]
)[
  Was wenn man nicht ganze 64bit aus dem Speicher laden möchte?
  - ```yasm lb $rd $offset($rsi)```
TODO visualisierung
  Was passiert mit den 56 freien Bits?
  + *Zero Extending*: Bei *lbu* (Load Byte unsinged) werden alle oberen Bits auf null gesetzt
  + *Sign Extending*
]

#slide(
  title: [Two's Complement]
)[
  (TODO zwischenergebnisse selber überlegen und aufdecken)
  - Ziel: Binärdarstellung von ganzen (insbesondere negativen) Zahlen mit $N$ Bits
  - Natürliche Zahlen: $(n : NN) mapsto "bin"(n) quad 6 mapsto 00000110$ (für $N = 8$)
  - Negative Zahlen: $-n mapsto "bin"(n)^(-1) + 1$ 
    - $6 mapsto (00000110)^(-1) + 1 mapsto 11111001 + 1 mapsto 11111010 $
  - Warum?(TODO aufdecken)
    - 0 ist eindeutig
    - Addition funktioniert selbst mit negativen Zahlen
    - TODO gibt bestimmt noch mehr
]

#slide(
  title: [Sign extending]
)[
  TODO an der Tafel beispiel machen
  Wir haben eine Zahl $n$ (als Binärzahl mit 8 Bits) und wollen diese in ein 64-Bit register Speichern (ohne den Wert zu ändern).
  - *Zero Extending*: Wenn die Zahl negativ ist, ändert sich der Wert
  - *Sign Extending*: Der neue Platz wird mit dem _MSB_ (Most significant Bit) aufgefüllt
    -  Bei Positiven Zahlen bleibt der Wert gleich (gleich wie Zero-Extending)
    -  Bei Negativen Zahlen: 
// https://t.yw.je/#N4Igdg9gJgpgziAXAbVABwnAlgFyxMJZABgBpiBdUkANwEMAbAVxiRABIwB9ADnZAC+pdJlz5CKMgEYqtRizbsAtNz6DhIDNjwEiU0jOr1mrRBxVcAbABZ+QkdvF7ys4wrOcrtwbJhQA5vBEoABmAE4QALZIZCA4EEgAzEbypiBwMAAeOOqhEdGI+nEJiABMKSZsGdm5IOFRMdTxSEVuaWAw-rX1BcnFSOVylWYdXQIUAkA
#align(center, commutative-diagram(
  node((0, 0), [$n_8$]),
  node((1, 0), [$-n_8$]),
  node((1, 1), [$-n_64$]),
  node((0, 1), [$n_64$]),
  arr((0, 0), (0, 1), [sext]),
  arr((1, 0), (1, 1), [sext]),
  arr((0, 0), (1, 0), [neg]),
  arr((0, 1), (1, 1), [neg]),
))
  Es ist egal, ob man zuerst negiert oder Sign extended. (TODO Beispiel)
]


#slide(
  title: [Speicher]
)[
  Was wenn man nicht ganze 64bit aus dem Speicher laden möchte?
  - ```yasm lb $rd $offset($rsi)```
TODO visualisierung
  Was passiert mit den 56 freien Bits?
  + *Zero Extending*: Bei *lbu* (Load Byte unsinged) werden alle oberen Bits auf null gesetzt
  + *Sign Extending*: Bei *lb* (Load Byte) wird der Wert auf 64-Bit Sign Extended und in _rd_ geschrieben.
  TODO aufdecken: Es gibt ähnliche Instructions für halfwords (16 Bit) und words (32 Bit).
]
TODO bei anderen Instructions (li addi etc) wird auch sign extended.
#slide(
  title: [Lui und auipc]
)[
  *li* kann nur einen 12-Bit Wert laden, was wenn man größere Zahlen möchte?
  - ```yasm lui $rd $imm``` (Load upper immediate): Setzt die unteren 12 Bits von _rd_ auf null und die nächsten 20 Bits auf _imm_.

]

#slide(
  title: [Control Flow Instructions]
)[
  - ```yasm jal $rd $offset``` (Jump and Link): Schreibe den Wert des Program Counters in _rd_ und erhöhe den Program Counter um _offset_
  - ```yasm beq $rs1 $rs2 $offset``` (Branch if equal): Wenn _rs1_ gleich _rs2_ ist, dann wird der Program Counter um _offset_ erhöht.
]

#slide(
  title: [Hello World.asm]
)[
Jetzt verstehen wir was passiert! (abgesehen von call...)
  #columns(2)[
  ```yasm
main:
... 
	li	a4,3
	li	a5,37
	addw	a5,a4,a5
	addiw	a5,a5,2
...
	call	printf
...

  ```

    #colbreak()
```c
#include <stdio.h>
int main() {
	int a = 3;
	int b = 37;
	int c = a + b;
	c += 2;
	printf("Hello World! %d\n",c);
	return 0;
}
```
  ]
]

#slide(
  title: [ELF Dateien]
)[
  Assembler konvertiert menschlich lesbaren Assembly Code in eine Ausführbare ELF Datei:
  
  ```sh
   ╰─λ file hello_world
hello_world: ELF 64-bit LSB executable, UCB RISC-V, soft-float ABI, version 1 (SYSV), statically linked, with debug_info, not stripped
  ```
  - *file* command gibt informationen zu einer Datei aus
]
#slide(title: [Elf Dateien])[
  TODO leute raten lassen
```
hello_world: ELF 64-bit LSB executable, UCB RISC-V, soft-float ABI, version 1 (SYSV), statically linked, with debug_info, not stripped
```
  - _hello_world_ ist eine ausführbare (*executable*) *ELF* Datei
  - *64-bit* Register
  - *LSB*: Least Significant Bit (little endian)
  - *RISC-V*
  - *soft-float ABI*: keine Hardware für float operationen
  - *statically linked*: Hängt nicht von anderen Bibliotheken ab
]
#slide(title: [andere ELF Dateien])[
```
python3.14: ELF 64-bit LSB pie executable, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, BuildID[sha1]=ddf03afb83452757f6df249b7608c7df7b476e25, for GNU/Linux 4.4.0, stripped
  ```
  - *pie* (Position independent executable): Prgoramm kann an belibieger Speicheradresse geladen werden
  - *x86-84*: Instruction set
  - *dynamically linked*: Der *interpreter* läd Bibliotheken befor die Datei ausgeführt wird
]

#slide(title: [ELF Dateien])[
TODO auf Wikiepdia artikel verlinken
https://en.wikipedia.org/wiki/Executable_and_Linkable_Format
- *Header*: Allgemeine Informationen zu der Datei, Entry point, Programm header Tabelle
- *Programm Headers*: Geben einen Teil der Elf Datei an, der an einen bestimmten Ort in den Arbeitsspeicher geladen werden soll
]
#slide(title: [Instructions])[
  == Wie werden Instructions codiert?
  - 4 Bytes (32-Bits)
  #image("instruction_types.png")
]

#slide(title: [Beispiel])[
  #image("Instruction_example.png")
  #image("addi.png")

]
#slide(title: [Beispiel])[
  #align(center)[
  #image("Instruction_example_2.png")
  #image("sra.png")]
]

#slide(title: [Bitwise Operationen])[
#columns(2)[
  *AND*:  $ &101101 \ \& #h(0.5em) &000111 \ &#line(length: 25%) \  = &000101 $
  *OR*: $ &101101 \   \| #h(0.5em) &000111 \ &#line(length: 25%) \  = &101111 $
  #colbreak()
  *Left Shift* (8-Bit werte): $ 00110101 << 3 = 10101000 $ 
  *Logical Right Shift* (8-Bit Werte): $ 10110101 >> 3 = 00010110 $ 
  *Arithmetic Right Shift* (8-Bit Werte): $ 10110101 >>> 3 = 11110110 $
    $ 00110101 >>> 3 = 00000110 $
  ]
]

#slide(
  title: [`a.out`]
)[
  Es gibt mehrere Methoden sich eine Binärdatei anzuschauen
  + `file a.out`
  + `readelf a.out` (TODO welche flags)
  + `objdump a.out` (TODO welche flags)
]


  

Zum Debugging:
-   qemu-linux-riscv64 installieren
-   qemu gdb installieren
-   schritt für schritt durch
-   TODO das muss man nicht selber machen, sondern auf VM (Franz??)

- TODO kurzer reminder zu hexadezimalzahlen und binärzahlen
- TODO erklären wie ecalls funktionieren
- TODO rv64i multilib compilen

- TODO Riscv Musl toolchain erklären (bruacht -a extension für threading sachen)
