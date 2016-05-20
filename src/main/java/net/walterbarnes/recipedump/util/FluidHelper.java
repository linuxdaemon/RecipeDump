package net.walterbarnes.recipedump.util;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;

/**
 * @author pahimar
 * Classes borrowed from ee3 (http://github.com/pahimar/Equivalent-Exchange-3/)
*/
public class FluidHelper
{
    public static Comparator<FluidStack> comparator = new Comparator<FluidStack>()
    {

        public int compare(FluidStack fluidStack1, FluidStack fluidStack2)
        {

            if (fluidStack1 != null)
            {
                if (fluidStack2 != null)
                {
                    //                    if (fluidStack1.fluidID == fluidStack2.fluidID)
                    if (FluidRegistry.getFluidID(fluidStack1.getFluid()) == FluidRegistry.getFluidID(fluidStack2.getFluid()))
                    {
                        if (fluidStack1.amount == fluidStack2.amount)
                        {
                            if (fluidStack1.tag != null)
                            {
                                if (fluidStack2.tag != null)
                                {
                                    return (fluidStack1.tag.hashCode() - fluidStack2.tag.hashCode());
                                }
                                else
                                {
                                    return -1;
                                }
                            }
                            else
                            {
                                if (fluidStack2.tag != null)
                                {
                                    return 1;
                                }
                                else
                                {
                                    return 0;
                                }
                            }
                        }
                        else
                        {
                            return (fluidStack1.amount - fluidStack2.amount);
                        }
                    }
                    else
                    {
                        return (FluidRegistry.getFluidID(fluidStack1.getFluid()) - FluidRegistry.getFluidID(fluidStack2.getFluid()));
                    }
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (fluidStack2 != null)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        }
    };

    public static int compare(FluidStack fluidStack1, FluidStack fluidStack2)
    {
        return comparator.compare(fluidStack1, fluidStack2);
    }

    public static String toString(FluidStack fluidStack)
    {
        if (fluidStack != null)
        {
            return String.format("%sxfluidStack.%s", fluidStack.amount, fluidStack.getFluid().getName());
        }

        return "fluidStack[null]";
    }
}
