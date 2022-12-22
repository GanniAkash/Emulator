import java.io.FileNotFoundException;
import java.util.*;

public class Core {
    private int pc;
    private final int freq;
    private final Integer[] stack = new Integer[2];
    private String w_reg, wdt;
    private String trisgpio_reg, option_reg;
    public String opcode;
    public final HashMap<String, String> prog_mem;
    public final HashMap<String, String> registers;
    public Core(String hex_file, int freq) throws FileNotFoundException {
        this.prog_mem = Initializer.init_mem(hex_file);
        if(prog_mem != null) this.registers = Initializer.init_registers();
        else throw new FileNotFoundException("File not there");
        this.freq = freq;
        this.trisgpio_reg = "00001111";
        this.option_reg = "11111111";
        this.wdt = "00000000";
        this.pc = 240;
        this.w_reg = "00";
        this.stack[0] = null;
        this.stack[1] = null;
        System.out.println(trisgpio_reg+"\t"+option_reg+"\t"+wdt+"\t"+this.freq);
    }

    public final String getTrisgpio_Reg() {return trisgpio_reg;}
    public final String getOption_Reg() {return option_reg;}
    public final String getW_Reg() {return w_reg;}
    public final int getPc() {return pc;}

    private void execute(String opcode){
        if (opcode.equals("000000000000")) {}
        else if(opcode.equals("000000000010")) option();
        else if(opcode.equals("000000000010")) clrwdt();
        else if(opcode.equals("000000000011")) sleep();
        else if (opcode.startsWith("000000000")) tris(opcode.substring(9));
        else if(opcode.startsWith("1001")) call(opcode.substring(4));
        else if(opcode.startsWith("1100")) movlw(opcode.substring(4));
        else if(opcode.startsWith("1000")) retlw(opcode.substring(4));
        else if(opcode.startsWith("1110")) andlw(opcode.substring(4));
        else if(opcode.startsWith("1101")) iorlw(opcode.substring(4));
        else if(opcode.startsWith("1111")) xorlw(opcode.substring(4));
        else if(opcode.startsWith("101")) gotok(opcode.substring(3));
        else if (opcode.startsWith("00")) {
            if(Objects.equals(opcode.substring(2, 7), "00001")) movwf(opcode.substring(7));
            else if (Objects.equals(opcode.substring(2, 6), "0001")) {
                if (opcode.charAt(6) == 'a') clrf(opcode.substring(7));
                else clrw();
            }
            else if(Objects.equals(opcode.substring(2, 6), "0011")) decf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1011")) decfsz(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "0111")) addwf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "0101")) andwf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1001")) comf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1010")) incf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1111")) incfsz(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "0100")) iorwf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1000")) movf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1101")) rlf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1100")) rrf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "0010")) subwf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "1110")) swapf(opcode.substring(6, 7), opcode.substring(7));
            else if(Objects.equals(opcode.substring(2, 6), "0110")) xorwf(opcode.substring(6, 7), opcode.substring(7));
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
        if (d.equals("0")) w_reg = String.format("%02x", f_val);
        else registers.put(f, s_f_val);
    }

    private void decfsz(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int f_val = Integer.parseInt(registers.get(f), 2);
        if (f_val == 0) f_val = 255;
        else f_val -= 1;
        if (f_val==0) pc += 1;
        String s_f_val = String.format("%8s", Integer.toBinaryString(f_val)).replace(' ', '0');
        if (d.equals("0")) w_reg = String.format("%02x", f_val);
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
        System.out.println("hi");
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

    private void addwf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int res = Integer.parseInt(w_reg, 8) + Integer.parseInt(registers.get(f), 2);
        if (res > 255) {
            res = res%255;
            bsf("0", Integer.toBinaryString(3));
        }
        else bcf("0", Integer.toBinaryString(3));
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        if(Initializer.dcarry_add(String.format("%8s", Integer.toBinaryString(Integer.parseInt(w_reg, 16))).replace(' ', '0'), registers.get(f))) bsf("1", Integer.toBinaryString(3));
        else bcf("1", Integer.toBinaryString(3));
        if (d.equals("0")) w_reg = String.format("%02x", res);
        else registers.put(f, String.format("%8s", Integer.toBinaryString(res)).replace(' ', '0'));
    }

    private void andwf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int res = Integer.parseInt(registers.get(f), 2) & Integer.parseInt(w_reg, 16);
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        if (d.equals("0")) w_reg = String.format("%02x", res);
        else registers.put(f, String.format("%8s", Integer.toBinaryString(res)).replace(' ', '0'));
    }

    private void comf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String f_val = registers.get(f);
        f_val = f_val.replace('0', '-');
        f_val = f_val.replace('1', '0');
        f_val = f_val.replace('-', '1');
        if (f_val.equals("00000000")) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        if (d.equals("0")) w_reg = String.format("%02x", Integer.parseInt(f_val, 2));
        else registers.put(f, f_val);
    }

    private void incf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int f_val = Integer.parseInt(registers.get(f), 2);
        if (f_val == 255) f_val = 0;
        else f_val += 1;
        if (f_val==0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        String s_f_val = String.format("%8s", Integer.toBinaryString(f_val)).replace(' ', '0');
        if (d.equals("0")) w_reg = String.format("%02x", f_val);
        else registers.put(f, s_f_val);
    }

    private void incfsz(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int f_val = Integer.parseInt(registers.get(f), 2);
        if (f_val == 255) f_val = 0;
        else f_val += 1;
        if (f_val==0) pc += 1;
        String s_f_val = String.format("%8s", Integer.toBinaryString(f_val)).replace(' ', '0');
        if (d.equals("0")) w_reg = String.format("%02x", f_val);
        else registers.put(f, s_f_val);
    }

    private void iorwf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int res = Integer.parseInt(registers.get(f), 2) | Integer.parseInt(w_reg, 16);
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        if (d.equals("0")) w_reg = String.format("%02x", res);
        else registers.put(f, String.format("%8s", Integer.toBinaryString(res)).replace(' ', '0'));
    }

    private void movf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        if (d.equals("0")) w_reg = String.format("%02x", Integer.parseInt(registers.get(f), 2));
        if (registers.get(f).equals("00000000")) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
    }

    private void rrf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String status = registers.get("03");
        String f_val = registers.get(f);
        char c = status.charAt(7);
        status = status.substring(0, 7) + f_val.charAt(7);
        f_val = c + f_val.substring(0, 7);
        if (d.equals("0")) w_reg = String.format("%02x", Integer.parseInt(f_val, 2));
        else registers.put(f, f_val);
    }

    private void rlf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String status = registers.get("03");
        String f_val = registers.get(f);
        char c = status.charAt(7);
        status = status.substring(0, 7) + f_val.charAt(0);
        f_val = f_val.substring(1) + c;
        if (d.equals("0")) w_reg = String.format("%02x", Integer.parseInt(f_val, 2));
        else registers.put(f, f_val);
    }

    private void subwf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        if (d.equals("1")) {
            String temp_w = String.valueOf(w_reg);
            comf(d, f);
            movlw("1");
            addwf(d, f);
            String temp_reg = registers.get("03");
            movlw(temp_w);
            registers.put("03", temp_reg);
        }
        else {
            String temp_f = registers.get(f);
            comf(d, f);
            movwf(f);
            movlw("1");
            addwf(d, f);
            registers.put(f, temp_f);
        }
    }

    private void swapf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        String f_val = registers.get(f);
        f_val = f_val.substring(4) + f_val.substring(0, 4);
        if (d.equals("0")) w_reg = String.format("%02x", Integer.parseInt(f_val, 2));
        else registers.put(f, f_val);
    }

    private void xorwf(String d, String f) {
        f = String.format("%02x", Integer.parseInt(f, 2));
        int res = Integer.parseInt(registers.get(f), 2) ^ Integer.parseInt(w_reg, 16);
        if (d.equals("0")) w_reg = String.format("%02x", res);
        else registers.put(f, String.format("%8s", Integer.toBinaryString(res)).replace(' ', '0'));
    }

    private void andlw(String k)  {
        int res = Integer.parseInt(k, 2);
        res = res & Integer.parseInt(w_reg, 16);
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        w_reg = String.format("%02x", res);
    }

    private void clrwdt() {
        bsf("3", Integer.toBinaryString(3));
        bsf("4", Integer.toBinaryString(3));
        wdt = "00000000";
        // Incomplete
    }

    private void iorlw(String k)  {
        int res = Integer.parseInt(k, 2);
        res = res | Integer.parseInt(w_reg, 16);
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        w_reg = String.format("%02x", res);
    }

    private void xorlw(String k)  {
        int res = Integer.parseInt(k, 2);
        res = res ^ Integer.parseInt(w_reg, 16);
        if (res == 0) bsf("2", Integer.toBinaryString(3));
        else bcf("2", Integer.toBinaryString(3));
        w_reg = String.format("%02x", res);
    }

    private void sleep() {
        bcf("3", Integer.toBinaryString(3));
        bsf("4", Integer.toBinaryString(3));
        wdt = "00000000";
        // Incomeplte
    }

    public final void run() {
        pc = 0;
        while(true) {
            step();
        }
    }

    public final void step() {
        String opcode, pcl;
        opcode = prog_mem.get(String.format("%04x", pc));
        opcode = String.format("%12s", Integer.toBinaryString(Integer.parseInt(opcode, 16))).replace(' ', '0');
        try {
            execute(opcode);
        }
        catch (final Exception e) {
        }
        if (pc >= 255) pc = 0;
        else pc += 1;
        pcl = String.format("%12s", Integer.toBinaryString(pc)).replace(' ', '0');
        pcl = pcl.substring(4);
        registers.put("02", pcl);
    }
}
