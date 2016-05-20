package net.walterbarnes.recipedump;

import com.google.gson.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.walterbarnes.recipedump.compat.Ic2Compat;
import net.walterbarnes.recipedump.stack.WrappedStack;
import net.walterbarnes.recipedump.util.RecipeHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(modid = RecipeDump.MODID, name = RecipeDump.MODNAME, version = RecipeDump.VERSION, dependencies = "after:MineTweaker3;after:modtweaker2;after:IC2")
public class RecipeDump {
    public static final String MODID = "recipedump";
    public static final String MODNAME = "Recipe Dump";
    public static final String VERSION = "@VERSION@";
    private JsonObject json = new JsonObject();
    public static JsonParser parser = new JsonParser();

    @Mod.Instance(MODID)
    public static RecipeDump instance;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) throws IOException {
        json.add("recipes", new JsonArray());

        dumpCrafting();
        dumpSmelting();

        if (Loader.isModLoaded("IC2"))
        {
            System.out.println("IC2 loaded");
            for (JsonElement elm : Ic2Compat.dump())
            {
                json.getAsJsonArray("recipes").add(elm);
            }
        }

        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        OutputStreamWriter fw = new OutputStreamWriter(
                new FileOutputStream(new File("recipes.json")),
                Charset.forName("UTF-8").newEncoder()
        );
        fw.append(gsonBuilder.toJson(json));
        fw.close();
    }

    private void dumpSmelting() {
        Map recipeObjMap = FurnaceRecipes.smelting().getSmeltingList();
        Map<ItemStack, ItemStack> recipes = (Map<ItemStack, ItemStack>)recipeObjMap;
        for (Map.Entry<ItemStack, ItemStack> recipe : recipes.entrySet())
        {
            ItemStack out = recipe.getValue();
            WrappedStack outWrapped = WrappedStack.wrap(out);
            JsonObject outJson = parser.parse(outWrapped.toJson()).getAsJsonObject();
            JsonArray outArray = new JsonArray();;
            outArray.add(outJson);

            ItemStack in = recipe.getKey();
            WrappedStack inWrapped = WrappedStack.wrap(in);
            JsonObject inJson = parser.parse(inWrapped.toJson()).getAsJsonObject();
            JsonArray inArray = new JsonArray();
            inArray.add(inJson);

            JsonObject recipeJson = new JsonObject();
            recipeJson.add("type", new JsonPrimitive("smelting"));
            recipeJson.add("outputs", outArray);
            recipeJson.add("ingredients", inArray);
            json.getAsJsonArray("recipes").add(recipeJson);
        }
    }

    private void dumpCrafting()
    {
        List recipes = CraftingManager.getInstance().getRecipeList();
        for (Object recipeObject : recipes)
        {
            assert recipeObject instanceof IRecipe;
            IRecipe recipe = (IRecipe) recipeObject;
            if (recipeObject instanceof ShapedRecipes || recipeObject instanceof ShapelessRecipes || recipeObject instanceof ShapedOreRecipe || recipeObject instanceof ShapelessOreRecipe)
            {
                ItemStack recipeOutput = recipe.getRecipeOutput();

                if (recipeOutput != null)
                {
                    List<WrappedStack> recipeInputs = RecipeHelper.getRecipeInputs(recipe);

                    if (!recipeInputs.isEmpty())
                    {
                        JsonArray in = new JsonArray();
                        for (WrappedStack ing : recipeInputs)
                        {
                            in.add(parser.parse(ing.toJson()).getAsJsonObject());
                        }
                        JsonArray out = new JsonArray();
                        out.add(parser.parse(WrappedStack.wrap(recipeOutput).toJson()).getAsJsonObject());
                        JsonObject recipeJson = new JsonObject();
                        recipeJson.add("type", new JsonPrimitive("crafting"));
                        recipeJson.add("outputs", out);
                        recipeJson.add("ingredients", in);
                        json.getAsJsonArray("recipes").add(recipeJson);
                        continue;
                    }
                }
            }
            if (Loader.isModLoaded("IC2"))
            {
                Optional<JsonObject> rOpt = Ic2Compat.getRecipe(recipe);
                if (rOpt.isPresent())
                {
                    json.getAsJsonArray("recipes").add(rOpt.get());
                }
            }
        }
    }
}
