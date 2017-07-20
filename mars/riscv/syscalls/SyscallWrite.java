package mars.riscv.syscalls;

import mars.ExitingException;
import mars.Globals;
import mars.ProgramStatement;
import mars.riscv.hardware.AddressErrorException;
import mars.riscv.hardware.RegisterFile;
import mars.riscv.AbstractSyscall;
import mars.util.SystemIO;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */


/**
 * Service to write to file descriptor given in a0.  a1 specifies buffer
 * and a2 specifies length.  Number of characters written is returned in a0.
 */

public class SyscallWrite extends AbstractSyscall {
    public SyscallWrite() {
        super("Write");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int byteAddress = RegisterFile.getValue("a1"); // source of characters to write to file
        int reqLength = RegisterFile.getValue("a2"); // user-requested length
        if (reqLength < 0) {
            RegisterFile.updateRegister("a0", -1);
            return;
        }
        int index = 0;
        byte myBuffer[] = new byte[RegisterFile.getValue("a2") + 1]; // specified length plus null termination
        try {
            byte b = (byte) Globals.memory.getByte(byteAddress);
            while (index < reqLength) // Stop at requested length. Null bytes are included.
            // while (index < reqLength && b != 0) // Stop at requested length OR null byte
            {
                myBuffer[index++] = b;
                byteAddress++;
                b = (byte) Globals.memory.getByte(byteAddress);
            }
            myBuffer[index] = 0; // Add string termination
        } // end try
        catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }
        int retValue = SystemIO.writeToFile(
                RegisterFile.getValue("a0"), // fd
                myBuffer, // buffer
                RegisterFile.getValue("a2")); // length
        RegisterFile.updateRegister("a0", retValue); // set returned value in register

        // Getting rid of processing exception.  It is the responsibility of the
        // user program to check the syscall's return value.  MARS should not
        // re-emptively terminate MIPS execution because of it.  Thanks to
        // UCLA student Duy Truong for pointing this out.  DPS 28-July-2009
         /*
         if (retValue < 0) // some error in opening file
         {
            throw new ProcessingException(statement,
                                    SystemIO.getFileErrorMessage());
         }
			*/
    }
}