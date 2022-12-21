import java.util.*;
import java.io.File;

public class Initializer {
    public static HashMap<String, String> init_mem(String hex_file) {
        HashMap<String, String> prog_mem = new HashMap<>();
        try {
            File file = new File(hex_file);
            Scanner reader = new Scanner(file);
            String line, rw, opcode;
            int line_length, addr_val;
            while (reader.hasNextLine()) {
                line = reader.nextLine().substring(1);
                line_length = Integer.parseInt(line.substring(0, 2), 16)/2;
                rw = line.substring(6, 8);
                addr_val = Integer.parseInt(line.substring(2, 6), 16)/2;
                if (rw.equals("00")) {
                    for (int i = 0; (i < line_length) && (addr_val+i)<256; i++) {
                        int j = 4*i;
                        opcode = line.substring(10+j, 12+j)+line.substring(8+j, 10+j);
                        prog_mem.put(String.format("%04x", addr_val+i), opcode);
                    }
                }
            }
            reader.close();
        }
        catch (Exception e) {
            return null;
        }
        for (int i=0; i<256; i++) {
            String addr = String.format("%04x", i);
            if (!prog_mem.containsKey(addr)) {
                prog_mem.put(addr, "0000");
            }
        }
        return prog_mem;
    }

    public static HashMap<String, String> init_registers() {
        HashMap <String, String> registers = new HashMap<>();
        registers.put("00", "00000000");
        registers.put("01", "00000000");
        registers.put("02", "11111111");
        registers.put("03", "00011000");
        registers.put("04", "11100000");
        registers.put("05", "11111110");
        registers.put("06", "00000000");
        registers.put("07", "11111111");
        for (int i = 8; i < 16; i++) {
            registers.put(String.format("%02x", i), null);
        }
        for (int i = 16; i < 32; i++) {
            registers.put(String.format("%02x", i), "00000000");
        }
        return registers;
    }

    public static boolean dcarry_add(String num1, String num2) {
        int i = num1.length()-1;
        while(i >= 4) {
            if (num1.charAt(i)=='1' && num2.charAt(i)=='1') return true;
            i -= 1;
        }
        return false;
    }

    public static boolean dcarry_sub(String num1, String num2) {
        int i = num1.length()-1;
        while(i >= 4) {
            if (num1.charAt(i)=='0' && num2.charAt(i)=='1') return true;
            i -= 1;
        }
        return false;
    }

    public static boolean carry_sub(String num1, String num2) {
        int i = num1.length()-1;
        while(i >= 0) {
            if (num1.charAt(i)=='0' && num2.charAt(i)=='1') return true;
            i -= 1;
        }
        return false;
    }
}
