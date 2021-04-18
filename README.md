chip8-emulator
=================

My attempt to create a CHIP-8 emulator.

### Requirements

- Java 11.

### How to run

Run `com/waldou/chip8/Main.java` class or build runnable `.jar` with `./gradlew clean jar`.

### Status

![Gif](https://github.com/waldou/chip8-emulator/blob/master/demo.gif)

- Fully working to the best of my knowledge, with at least the original 34 opcodes (0x0NNN is not implemented).
- Potential improvements:
    * Better test coverage.
    * Additional graphic effects.

#### Keys

**ORIGINAL**:

- 1 2 3 C
- 4 5 6 D
- 7 8 9 E
- A 0 B F

**KEYBOARD MAPPING**:

- 1 2 3 4
- Q W E R
- A S D F
- Z X C V

**OTHER KEYS**:

- Esc: Close ROM.
- Backspace: Reset ROM.
- Right Arrow Key: Switch color theme.

### References

https://en.wikipedia.org/wiki/CHIP-8

http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#8xy3

http://multigesture.net/wp-content/uploads/mirror/goldroad/chip8.shtml

http://multigesture.net/articles/how-to-write-an-emulator-chip-8-interpreter/

https://chip-8.github.io/extensions/

https://github.com/trapexit/chipce8/blob/master/docs/CHIP-8/Misc/VP580%2C%20VP585%2C%20VP590%2C%20VP595%20Instruction%20Manual%20Including%20CHIP-8X.pdf