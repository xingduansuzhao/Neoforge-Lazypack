# AGENTS.md

This project is a NeoForge 1.21.10 Minecraft mod. When adding complex custom weapons with first-person and third-person animations, follow the workflow below. This file exists because the rendering/animation pipeline is easy to misunderstand: Blockbench preview can look correct while the in-game first-person or third-person item renderer uses a different path.

## Core Rule

Use three separate Blockbench outputs for each weapon:

1. GeckoLib Item project  
   Handles the weapon's actual GeckoLib item model, item display settings, and first-person weapon animation.

2. Player/Steve animation project  
   Handles PAL third-person player arm/body animation only.

3. Java Block/Item project  
   Handles third-person item display correction only, especially `thirdperson_righthand` and `thirdperson_lefthand`.

Do not try to make one exported model serve all three jobs. First-person and third-person rendering are different pipelines.

## Successful Architecture

For a weapon such as `qingtian`, the stable setup is:

- Third-person item render:
  - Use Java Block/Item model through `minecraft:model`.
  - Only for `thirdperson_righthand` and `thirdperson_lefthand`.
  - This avoids GeckoLib item pivot/display mismatch in Minecraft's third-person item layer.

- First-person item render:
  - Use GeckoLib special item renderer.
  - This preserves GeckoLib item animation such as first-person heavy attack.

- Third-person player animation:
  - Use Player Animation Library (PAL).
  - PAL animates player bones such as `right_arm` and `left_arm`.
  - PAL does not directly render the weapon model from the Blockbench player-preview model.

The item definition must use `minecraft:display_context` to split rendering:

```json
{
  "model": {
    "type": "minecraft:select",
    "cases": [
      {
        "model": {
          "type": "minecraft:model",
          "model": "aimod:item/<weapon>_third_person"
        },
        "when": [
          "thirdperson_lefthand",
          "thirdperson_righthand"
        ]
      }
    ],
    "fallback": {
      "type": "minecraft:special",
      "base": "aimod:item/<weapon>",
      "model": {
        "type": "geckolib:geckolib"
      }
    },
    "property": "minecraft:display_context"
  }
}
```

## Blockbench Exports To Request From The User

For each new weapon, ask the user for these files.

### 1. GeckoLib Item Project

Purpose: first-person weapon model, item display, and first-person weapon animation.

Expected exports:

- `<weapon>-geckolib-model.geo.json`
- `<weapon>-geckolib-display.json`
- `<weapon>.zhongji.json` or another first-person animation JSON
- `<weapon>.png`

Notes:

- The GeckoLib model must be exported from the GeckoLib Item project.
- The first-person animation must target bones that exist in the GeckoLib geo model, commonly `root`.
- The display file may include third-person values, but in the stable split pipeline those third-person values are ignored because third-person rendering uses Java Block/Item.

### 2. Player/Steve Animation Project

Purpose: third-person player animation.

Expected exports:

- `<weapon>_animations.json`
- Optional reference only: `<weapon>.zhongji.model.json`
- Optional source project: `<weapon>_steve.bbmodel`

Notes:

- The animation JSON is for PAL.
- It should contain the animation key exported by Blockbench, often `test`; the project should rename/map it to `heavy_attack` when copying into resources.
- PAL uses the animation key inside the JSON. In this project, the heavy attack ResourceLocation is `aimod:heavy_attack`, so the resource should contain:

```json
{
  "animations": {
    "heavy_attack": { ... }
  }
}
```

- `qingtian.zhongji.model.json` or similar full player+weapon model files are only diagnostic/reference files. The game does not directly render that model for the held item.

### 3. Java Block/Item Project

Purpose: third-person weapon display correction.

Expected exports:

- `<weapon>-JavaBlockItem.json`
- The same `<weapon>.png` texture if not already provided

Notes:

- This model is used only for third-person item contexts.
- The important display keys are:
  - `thirdperson_righthand`
  - `thirdperson_lefthand`
- The model should look correct in third-person hand display in Blockbench.
- This export is not used for first-person GeckoLib animation.

## Resource Layout

For a weapon named `<weapon>`:

```text
src/main/resources/assets/aimod/items/<weapon>.json
src/main/resources/assets/aimod/models/item/<weapon>.json
src/main/resources/assets/aimod/models/item/<weapon>_third_person.json
src/main/resources/assets/aimod/geckolib/models/item/<weapon>.geo.json
src/main/resources/assets/aimod/geckolib/animations/item/<weapon>.animation.json
src/main/resources/assets/aimod/player_animations/<weapon>_animations.json
src/main/resources/assets/aimod/textures/item/<weapon>.png
```

Meanings:

- `items/<weapon>.json`: item-model dispatch. Must split third-person and fallback GeckoLib special renderer.
- `models/item/<weapon>.json`: base model used by GeckoLib special renderer. It should usually be `parent: "builtin/entity"` and contain display data from the GeckoLib display export.
- `models/item/<weapon>_third_person.json`: Java Block/Item model exported for third-person hand rendering. Texture paths must use the project namespace, e.g. `aimod:item/<weapon>`.
- `geckolib/models/item/<weapon>.geo.json`: copied from `<weapon>-geckolib-model.geo.json`.
- `geckolib/animations/item/<weapon>.animation.json`: copied from the GeckoLib first-person item animation export.
- `player_animations/<weapon>_animations.json`: copied from the player animation export, with internal animation key mapped to `heavy_attack` or the expected ResourceLocation path.

## Implementation Notes

### Item Class

The weapon item can implement `GeoItem` for first-person GeckoLib item animation.

On right click:

- Server side: trigger the GeckoLib item animation.
- Client side: trigger the PAL player animation.

Also override vanilla swing if needed:

```java
@Override
public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
    return true;
}
```

This prevents vanilla swing from fighting the custom animation.

### PAL Animation

PAL is for player bones. It does not automatically render the weapon model from a Blockbench player-preview file.

Use a controller layer such as:

```java
public static final ResourceLocation LAYER_ID =
        ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "<weapon>_heavy_attack_layer");

public static final ResourceLocation HEAVY_ATTACK =
        ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "heavy_attack");
```

Then call `triggerAnimation(HEAVY_ATTACK)` on right click.

### GeckoLib Item Animation

The first-person weapon animation file may use an internal animation name such as `test`. If so, the Java item class should trigger that GeckoLib animation name:

```java
private static final RawAnimation ZHONGJI = RawAnimation.begin().thenPlay("test");
```

Do not rename the animation inside the user's source file unless explicitly requested. It is okay to copy it into project resources.

## Important Pitfalls

### Do Not Use GeckoLib Item Renderer For Third-Person If The Weapon Drifts

GeckoLib item renderer has its own model pivot and render baseline. Minecraft third-person item layer has its own hand transform. PAL has its own player bone transform.

Combining all three often produces offset or drifting weapons during third-person animation, even if Blockbench preview looks perfect.

The stable fix is third-person Java Block/Item rendering via `display_context` split.

### Blockbench Player Preview Is Not The Same As Game Third-Person Item Rendering

In Blockbench, a preview model may have:

```text
right_arm
  -> right_item
      -> root
          -> weapon
```

But the game does not render `qingtian.zhongji.model.json` as the held item. PAL only reads animation values from the exported animation JSON. The actual held item is still rendered by Minecraft's item layer.

### `right_item` / `left_item` Are Not A Magic Fix

PAL has hooks for `right_item` and `left_item`, but those bones are only extra transforms for the item layer.

If exported `right_item` and `left_item` keyframes are all zero, they do not fix anything.

However, manually tuning them is difficult and not recommended as the primary workflow for these weapons. Prefer third-person Java Block/Item rendering.

### Do Not Mechanically Convert Java Block/Item To GeckoLib Geo

A mechanical conversion loses the exact Blockbench GeckoLib coordinate/pivot assumptions. Always use the actual GeckoLib export for `geckolib/models/item/<weapon>.geo.json`.

### Do Not Edit User Export Files In Place

External files in the Blockbench folder are source exports. Copy them into project resources and transform the project copy as needed.

## Current Qingtian Example

The working `qingtian` setup follows this pattern:

- Third-person item model:
  - Source export: `qingtian-JavaBlockItem.json`
  - Project resource: `assets/aimod/models/item/qingtian_third_person.json`

- First-person/other GeckoLib item model:
  - Source export: `qingtian-geckolib-model.geo.json`
  - Project resource: `assets/aimod/geckolib/models/item/qingtian.geo.json`

- GeckoLib item display:
  - Source export: `qingtian-geckolib-display.json`
  - Project resource: `assets/aimod/models/item/qingtian.json`

- GeckoLib item animation:
  - Source export: `qingtian.zhongji.json`
  - Project resource: `assets/aimod/geckolib/animations/item/qingtian.animation.json`

- PAL player animation:
  - Source export: `qingtian_animations.json`
  - Project resource: `assets/aimod/player_animations/qingtian_animations.json`
  - Internal key mapped to `heavy_attack`

- Item definition:
  - Project resource: `assets/aimod/items/qingtian.json`
  - Uses `minecraft:display_context` to route only third-person hand contexts to `qingtian_third_person`; all other contexts fallback to GeckoLib special renderer.

## Verification Checklist

After implementing a weapon:

- Run `./gradlew build`.
- Confirm the jar contains:
  - `assets/aimod/items/<weapon>.json`
  - `assets/aimod/models/item/<weapon>.json`
  - `assets/aimod/models/item/<weapon>_third_person.json`
  - `assets/aimod/geckolib/models/item/<weapon>.geo.json`
  - `assets/aimod/geckolib/animations/item/<weapon>.animation.json`
  - `assets/aimod/player_animations/<weapon>_animations.json`
  - `assets/aimod/textures/item/<weapon>.png`
- In-game test:
  - First-person idle pose.
  - First-person right-click animation.
  - Third-person idle held pose.
  - Third-person right-click/player animation.
  - GUI/ground/fixed if relevant.

If first-person works but third-person weapon drifts, check that `items/<weapon>.json` is still splitting third-person to `<weapon>_third_person` and has not been changed back to all-GeckoLib rendering.

