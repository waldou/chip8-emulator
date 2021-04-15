chip8-emulator
=================

My attempt to create a CHIP-8 emulator.

### Requirements

- Java 11.

### How to run

Run `Main.java` with first argument as the ROM file path.

### Status

- Fully working to the best of my knowledge, with at least the original 34 opcodes (minus 0x0NNN).
- Potential improvements:
    * Better test coverage.
    * Menu for rom load.
    * Reset rom.
    * Save states.
    * Color themes.
    * Additional graphic effects.

### References

https://en.wikipedia.org/wiki/CHIP-8

http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#8xy3

http://multigesture.net/wp-content/uploads/mirror/goldroad/chip8.shtml

http://multigesture.net/articles/how-to-write-an-emulator-chip-8-interpreter/

https://chip-8.github.io/extensions/

https://github.com/trapexit/chipce8/blob/master/docs/CHIP-8/Misc/VP580%2C%20VP585%2C%20VP590%2C%20VP595%20Instruction%20Manual%20Including%20CHIP-8X.pdf