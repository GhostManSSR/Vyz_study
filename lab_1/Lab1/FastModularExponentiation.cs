using System;
namespace Lab1;

public class FastModularExponentiation
{
    private int _lenAction;
    
    public long Solver(long parameterA, long parameterX, long parameterP)
    {
        string binary = Convert.ToString(parameterX, 2);
        _lenAction = binary.Length;

        long result = 1;
        long current = parameterA % parameterP;

        for (var i = 0; i < _lenAction; i++)
        {
            int bit = binary[_lenAction - 1 - i] - '0';

            if (bit == 1)
            {
                result = result * current % parameterP;
            }

            current = current * current % parameterP;
        }

        return result;
        
    }


}