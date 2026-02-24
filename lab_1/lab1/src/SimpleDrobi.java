public class SimpleDrobi {
    private long chislitel;
    private long znamenatel;

    public SimpleDrobi(long a, long b) {
        if (b == 0) {
            throw new IllegalArgumentException("Знаменатель не может быть 0");
        }
        this.chislitel = a;
        this.znamenatel = b;
    }

    public SimpleDrobi(long a) {
        this(a, 1);
    }

    public SimpleDrobi() {
        this(0, 1);
    }

    public SimpleDrobi abs() {
        return new SimpleDrobi(Math.abs(chislitel), Math.abs(znamenatel));
    }

    public boolean isZero() {
        return chislitel == 0;
    }

    public long getChislitel() { return chislitel; }
    public long getZnamenatel() { return znamenatel; }

    public long compareTo(SimpleDrobi other) {
        long left = this.chislitel * other.znamenatel;
        long right = other.chislitel * this.znamenatel;
        return Long.compare(left, right);
    }

    @Override
    public String toString() {
        this.sokrashenie();
        if (znamenatel == 1) {
            return Long.toString(chislitel);
        }

        if (chislitel * znamenatel < 0) {
            return "-" + Math.abs(chislitel) + "/" + Math.abs(znamenatel);
        }
        return Math.abs(chislitel) + "/" + Math.abs(znamenatel);
    }
    public SimpleDrobi plusminus(SimpleDrobi other, boolean flag) {
        long commonZnamenatel = this.znamenatel * other.znamenatel;
        long newChislitel;
        if (flag) {
            newChislitel = this.chislitel * other.znamenatel + other.chislitel * this.znamenatel;
        } else {
            newChislitel = this.chislitel * other.znamenatel - other.chislitel * this.znamenatel;
        }
        SimpleDrobi result = new SimpleDrobi(newChislitel, commonZnamenatel);
        result.sokrashenie();
        return result;
    }

    public SimpleDrobi umndel(SimpleDrobi other, boolean flag) {
        long newChislitel;
        long newZnamenatel;
        if (flag) {
            newChislitel = this.chislitel * other.chislitel;
            newZnamenatel = this.znamenatel * other.znamenatel;
        } else {
            newChislitel = this.chislitel * other.znamenatel;
            newZnamenatel = this.znamenatel * other.chislitel;
        }
        SimpleDrobi result = new SimpleDrobi(newChislitel, newZnamenatel);
        result.sokrashenie();
        return result;
    }

    public SimpleDrobi plus(SimpleDrobi other) {
        return this.plusminus(other, true);
    }

    public SimpleDrobi minus(SimpleDrobi other) {
        return this.plusminus(other, false);
    }

    public SimpleDrobi umn(SimpleDrobi other) {
        return this.umndel(other, true);
    }

    public SimpleDrobi del(SimpleDrobi other) {
        return this.umndel(other, false);
    }

    private void sokrashenie() {
        long gcd = NOD(Math.abs(chislitel), Math.abs(znamenatel));
        chislitel /= gcd;
        znamenatel /= gcd;

        if (znamenatel < 0) {
            chislitel = -chislitel;
            znamenatel = -znamenatel;
        }
    }

    private long NOD(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

}
