package org.vivecraft.compat_impl.mc_1_14;

import org.vivecraft.accessors.AttributeModifier$OperationMapping;
import org.vivecraft.compat_impl.mc_1_13_2.NMS_1_13_2;
import org.vivecraft.util.reflection.ReflectionField;

import java.util.Collection;

public class NMS_1_14 extends NMS_1_13_2 {
    protected ReflectionField AttributeModifierOperation_ADD_VALUE;
    protected ReflectionField AttributeModifierOperation_ADD_MULTIPLIED_TOTAL;

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.AttributeModifierOperation_ADD_VALUE = ReflectionField.getField(
            AttributeModifier$OperationMapping.FIELD_ADD_VALUE);
        this.AttributeModifierOperation_ADD_MULTIPLIED_TOTAL = ReflectionField.getField(
            AttributeModifier$OperationMapping.FIELD_ADD_MULTIPLIED_TOTAL);
    }

    @Override
    protected double applyAttributeModifiers(double original, Collection<Object> modifiers) {
        for (Object modifier : modifiers) {
            double amount = (double) this.AttributeModifier_getAmount.invoke(modifier);
            Object operation = this.AttributeModifier_getOperation.invoke(modifier);
            if (operation == this.AttributeModifierOperation_ADD_VALUE.get()) {
                original += amount;
            } else if (operation == this.AttributeModifierOperation_ADD_MULTIPLIED_TOTAL.get()) {
                original += amount * original;
            }
        }
        return original;
    }
}
