package net.walterbarnes.recipedump.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import ic2.api.recipe.*;
import ic2.core.AdvRecipe;
import ic2.core.AdvShapelessRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.walterbarnes.recipedump.OreStack;
import net.walterbarnes.recipedump.RecipeDump;
import net.walterbarnes.recipedump.WrappedStack;
import net.walterbarnes.recipedump.util.RecipeHelper;

import java.util.*;

public class Ic2Compat {
    public static JsonArray dump() {
        JsonArray out = new JsonArray();
        for (JsonElement elm : Machines.Macerator.dump()) {
            out.add(elm);
        }
        return out;
    }

    public static Optional<JsonObject> getRecipe(IRecipe iRecipe) {
        JsonObject recipeJson = null;
        if (iRecipe instanceof AdvRecipe || iRecipe instanceof AdvShapelessRecipe) {
            ItemStack recipeOutput = iRecipe.getRecipeOutput();

            if (recipeOutput != null) {
                List<WrappedStack> recipeInputs = getInputs(iRecipe);

                if (!recipeInputs.isEmpty()) {
                    recipeJson = new JsonObject();
                    recipeJson.add("type", new JsonPrimitive("IC2:crafting"));
                    JsonArray outArr = new JsonArray();
                    JsonArray inArr = new JsonArray();
                    for (WrappedStack input : recipeInputs) {
                        inArr.add(RecipeDump.parser.parse(input.toJson()).getAsJsonObject());
                    }
                    outArr.add(RecipeDump.parser.parse(WrappedStack.wrap(recipeOutput).toJson()).getAsJsonObject());

                    recipeJson.add("outputs", outArr);
                    recipeJson.add("ingredients", inArr);
                }
            }
        }
        return Optional.ofNullable(recipeJson);
    }

    private static List<WrappedStack> getInputs(IRecipe recipe) {
        List<WrappedStack> recipeInputs = new ArrayList<>();

        if (recipe instanceof AdvRecipe) {
            recipeInputs = getWrappedInputs(((AdvRecipe) recipe).input);
        } else if (recipe instanceof AdvShapelessRecipe) {
            recipeInputs = getWrappedInputs(((AdvShapelessRecipe) recipe).input);
        }
        return RecipeHelper.collateInputStacks(recipeInputs);
    }

    private static List<WrappedStack> getWrappedInputs(Object[] inputs) {
        List<WrappedStack> recipeInputs = new ArrayList<>();
        for (Object object : inputs) {
            if (object instanceof RecipeInputItemStack) {
                RecipeInputItemStack input = (RecipeInputItemStack) object;
                ItemStack itemStack = input.input.copy();

                if (itemStack.stackSize > 1) {
                    itemStack.stackSize = 1;
                }

                recipeInputs.add(WrappedStack.wrap(itemStack));
            } else if (object instanceof RecipeInputOreDict) {
                RecipeInputOreDict input = (RecipeInputOreDict) object;
                recipeInputs.add(WrappedStack.wrap(new OreStack(input.input)));
            } else if (object instanceof List) {
                List list = (List) object;
                Object listObject = list.get(0);
                if (listObject instanceof RecipeInputItemStack) {
                    RecipeInputItemStack input = (RecipeInputItemStack) listObject;
                    ItemStack itemStack = input.input.copy();

                    if (itemStack.stackSize > 1) {
                        itemStack.stackSize = 1;
                    }
                    recipeInputs.add(WrappedStack.wrap(itemStack));
                } else if (listObject instanceof RecipeInputOreDict) {
                    RecipeInputOreDict input = (RecipeInputOreDict) listObject;
                    recipeInputs.add(WrappedStack.wrap(new OreStack(input.input)));
                }
            }
        }
        return recipeInputs;
    }

    public static final class Machines {
        public static final class Macerator {
            public static JsonArray dump() {
                JsonArray out = new JsonArray();

                for (Map.Entry<IRecipeInput, RecipeOutput> recipe : Recipes.macerator.getRecipes().entrySet()) {
                    JsonObject recipeJson = new JsonObject();
                    recipeJson.add("type", new JsonPrimitive("IC2:macerator"));
                    List<ItemStack> inputs = recipe.getKey().getInputs();
                    List<ItemStack> outputs = recipe.getValue().items;

                    if (outputs.size() > 0) {
                        List<WrappedStack> recipeInputs = getInputs(inputs);
                        if (recipeInputs != null) {
                            JsonArray outArr = new JsonArray();
                            for (ItemStack stack : outputs) {
                                outArr.add(RecipeDump.parser.parse(WrappedStack.wrap(stack).toJson()).getAsJsonObject());
                            }
                            JsonArray inArr = new JsonArray();
                            for (WrappedStack stack : recipeInputs) {
                                inArr.add(RecipeDump.parser.parse(stack.toJson()).getAsJsonObject());
                            }

                            recipeJson.add("outputs", outArr);
                            recipeJson.add("ingredients", inArr);
                            out.add(recipeJson);
                        }
                    }
                }
                return out;
            }

            private static List<WrappedStack> getInputs(List<ItemStack> input) {
                if (input.size() == 1) {
                    return RecipeHelper.collateInputStacks(Collections.singletonList(WrappedStack.wrap(input.get(0))));
                } else if (input.size() > 1) {
                    if (OreStack.getOreStackFromList(input) != null) {
                        return RecipeHelper.collateInputStacks(Collections.singletonList(WrappedStack.wrap(OreStack.getOreStackFromList(input))));
                    }
                }
                return null;
            }
        }
    }
}
