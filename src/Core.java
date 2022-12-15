import java.util.*;

public class Core {
    private int pc;
    private final int freq;
    private final Integer[] stack = new Integer[2];
    private String trisgpio_reg, option_reg, w_reg;
    private final HashMap<String, String> prog_mem;
    private final HashMap<String, String> registers;
    public Core(String hex_file, int freq) {
        this.prog_mem = Initializer.init_mem(hex_file);
        this.registers = Initializer.init_registers();
        this.freq = freq;
        this.trisgpio_reg = "00001111";
        this.option_reg = "11111111";
        this.pc = 240;
        this.w_reg = "00";
        this.stack[0] = null;
        this.stack[1] = null;
    }

    private void execute(String opcode){
        if (opcode.equals("000000000000")) {assert true;}
        else if(opcode.equals("000000000010")) option();
        else if (opcode.startsWith("000000000")) tris(opcode.substring(9));
        else if(opcode.startsWith("1001")) call(opcode.substring(4));
        else if(opcode.startsWith("1100")) movlw(opcode.substring(4));
        else if(opcode.startsWith("1000")) retlw(opcode.substring(4));
        else if(opcode.startsWith("101")) gotok(opcode.substring(3));
        else if (opcode.startsWith("00")) {
            if (Objects.equals(opcode.substring(2, 6), "0001")) {
                if (opcode.charAt(6) == 'a') clrf(opcode.substring(7));
                else clrw();
            }
            else if(Objects.equals(opcode.substring(2, 6), "0011")) decf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1011")) decfsz(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 7), "00001")) movwf(opcode.substring(7));
        }
        else if(opcode.startsWith("01")) {
            opcode = opcode.substring(2);
            if (opcode.startsWith("00")) bcf(opcode.substring(4, 7), opcode.substring(7));
            else if(opcode.startsWith("01")) bsf(opcode.substring(4, 7), opcode.substring(7));
            else if(opcode.startsWith("10")) btfsc(opcode.substring(4, 7), opcode.substring(7));
            else if(opcode.startsWith("11")) btfss(opcode.substring(4, 7), opcode.substring(7));
        }
    }

    private void bcf(String b, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String f_val = registers.get(f);
        b = b.substring(2)+b.charAt(1)+b.charAt(0);
        int bit = Integer.parseInt(b, 2);
        f_val = f_val.substring(0, 7-bit)+'0'+f_val.substring(8-bit);
        registers.put(f, f_val);
    }

    private void bsf(String b, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        b = b.substring(2)+b.charAt(1)+b.charAt(0);
        String f_val = registers.get(f);
        int bit = Integer.parseInt(b, 2);
        f_val = f_val.substring(0, 7-bit)+'1'+f_val.substring(8-bit);
        registers.put(f, f_val);
    }

    private void btfsc(String b, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        b = b.substring(2)+b.charAt(1)+b.charAt(0);
        int bit = Integer.parseInt(b, 2);
        String f_val = registers.get(f);
        if (f_val.charAt(7-bit) == '0'){
            if(pc == 255) pc = 0;
            else pc += 1;
        }
    }

    private void btfss(String b, String f){
        f = String.format("%02x", Integer.parseInt(f, 2));
        b = b.substring(2)+b.charAt(1)+b.charAt(0);
        int bit = Integer.parseInt(b, 2);
        String f_val = registers.get(f);
        if (f_val.charAt(7-bit) == '1'){
            if(pc == 255) pc = 0;
            else pc += 1;
        }
    }

    private void clrf(String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        registers.put(f, "00000000");
        bsf("2", Integer.toBinaryString(3));
    }

    private void clrw() {
        w_reg = "00";
        bsf("2", Integer.toBinaryString(3));
    }

    private void decf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int f_val = Integer.parseInt(registers.get(f), 2);
        if (f_val == 0) f_val = 255;
        else f_val -= 1;
        if (f_val==0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        String s_f_val = String.format("%8s", Integer.toBinaryString(f_val)).replace(' ', '0');
        if (d.equals("0")) w_reg = s_f_val;
        else registers.put(f, s_f_val);
    }

    private void decfsz(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int f_val = Integer.parseInt(registers.get(f), 2);
        if (f_val == 0) f_val = 255;
        else f_val -= 1;
        if (f_val==0) {
            if(pc == 255) pc = 0;
            else pc += 1;
        }
        String s_f_val = String.format("%8s", Integer.toBinaryString(f_val)).replace(' ', '0');
        if (d.equals("0")) w_reg = s_f_val;
        else registers.put(f, s_f_val);
    }

    private void movwf(String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String f_val = String.format("%8s", Integer.toBinaryString(Integer.parseInt(w_reg, 16))).replace(' ', '0');
        registers.put(f, f_val);
    }

    private void call(String k) {
        stack[1] = stack[0];
        stack[0] = pc;
        pc = Integer.parseInt(k, 2) - 1;
    }

    private void gotok(String k) {
        pc = Integer.parseInt(k, 2) - 1;
    }

    private void movlw(String k) {
        k = String.format("%02x", Integer.parseInt(k, 2));
        w_reg = k;
    }

    private void option() {
        option_reg = String.format("%8s", Integer.toBinaryString(Integer.parseInt(w_reg, 16))).replace(' ', '0');
    }

    private void tris(String f) {
        if (f.equals("110") || f.equals("111")){
            trisgpio_reg = String.format("%8s", Integer.toBinaryString(Integer.parseInt(w_reg, 16))).replace(' ', '0');
        }
    }

    private void retlw(String k) {
        w_reg = String.format("%02x", Integer.parseInt(k, 2));
        pc = stack[0];
        stack[0] = stack[1];
        stack[1] = null;
    }

    public final void start() {
        System.out.println(freq);
        String opcode, pcl;
        while(true) {
            opcode = prog_mem.get(String.format("%04x", pc));
            opcode = String.format("%12s", Integer.toBinaryString(Integer.parseInt(opcode, 16))).replace(' ', '0');
            try {
                execute(opcode);
            }
            catch (final Exception e) {
                break;
            }
            if (pc == 255) pc = 0;
            else pc += 1;
            pcl = String.format("%12s", Integer.toBinaryString(pc)).replace(' ', '0');
            pcl = pcl.substring(4);
            registers.put("02", pcl);
        }
    }

    public static void main(String[] args) {
        Core pic = new Core("src/test.hex", 0);
        pic.start();
    }
}
