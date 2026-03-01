package org.vmstudio.visor.compatibility;

import lombok.Getter;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public enum BlockClassifier {
    FARMABLE_BLOCK(
            (block)-> block instanceof CropBlock
                    || block instanceof StemBlock
                    || block instanceof AttachedStemBlock
    );

    @Getter
    private final List<Predicate<Block>> recognizers;
    @Getter
    private final List<Predicate<Block>> filters;
    BlockClassifier(Predicate<Block> defRecognizer){
        recognizers = new ArrayList<>();
        filters = new ArrayList<>();
        recognizers.add(defRecognizer);
    }
    public boolean is(Block block){
        boolean recognized = false;
        for(Predicate<Block> entry : recognizers){
            if(entry.test(block)){
                recognized = true;
                break;
            }
        }
        for(Predicate<Block> entry : filters){
            if(!entry.test(block)){
                recognized = false;
                break;
            }
        }

        return recognized;
    }
}
