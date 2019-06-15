package de.hhu.stups.codegenerator.generators;


import de.hhu.stups.codegenerator.handlers.NameHandler;
import de.hhu.stups.codegenerator.handlers.TemplateHandler;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.BoolType;
import de.prob.parser.ast.types.CoupleType;
import de.prob.parser.ast.types.DeferredSetElementType;
import de.prob.parser.ast.types.EnumeratedSetElementType;
import de.prob.parser.ast.types.IntegerType;
import de.prob.parser.ast.types.RecordType;
import de.prob.parser.ast.types.SetType;
import de.prob.parser.ast.types.StringType;
import de.prob.parser.ast.types.UntypedType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

public class TypeGenerator {

    private final STGroup group;

    private final NameHandler nameHandler;

    private RecordGenerator  recordGenerator;

    public TypeGenerator(final STGroup group, final NameHandler nameHandler) {
        this.group = group;
        this.nameHandler = nameHandler;
    }

    /*
    * This function generates code for a type with the given type and the information whether the type is generated for casting an object
    */
    public String generate(BType type) {
        if(type instanceof IntegerType) {
            return generateBInteger();
        } else if(type instanceof BoolType) {
            return generateBBoolean();
        } else if(type instanceof StringType) {
            return generateBString();
        } else if(type instanceof SetType) {
            return generateBSet((SetType) type);
        } else if(type instanceof EnumeratedSetElementType) {
            return generateEnumeratedSetElement((EnumeratedSetElementType) type);
        } else if(type instanceof DeferredSetElementType) {
            return generateDeferredSetElement((DeferredSetElementType) type);
        } else if(type instanceof CoupleType) {
            return generateBTuple((CoupleType) type);
        } else if(type instanceof RecordType) {
            return generateBStruct((RecordType) type);
        } else if(type instanceof UntypedType) {
            return generateUntyped();
        }
        return "";
    }

    private String generateBInteger() {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", "BInteger");
        return template.render();
    }

    private String generateBBoolean() {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", "BBoolean");
        return template.render();
    }

    private String generateBString() {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", "BString");
        return template.render();
    }

    private String generateBTuple(CoupleType type) {
        ST template = group.getInstanceOf("tuple_type");
        TemplateHandler.add(template, "leftType", generate(type.getLeft()));
        TemplateHandler.add(template, "rightType", generate(type.getRight()));
        return template.render();
    }

    private String generateBStruct(RecordType type) {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", recordGenerator.getStruct(type));
        return template.render();
    }

    private String generateBSet(SetType type) {
        BType subType = type.getSubType();
        if(subType instanceof CoupleType) {
            return generateBRelation((CoupleType) subType);
        } else {
            ST template = group.getInstanceOf("set_type");
            TemplateHandler.add(template, "type", generate(subType));
            return template.render();
        }
    }

    private String generateBRelation(CoupleType type) {
        ST template = group.getInstanceOf("relation_type");
        TemplateHandler.add(template, "leftType", generate(type.getLeft()));
        TemplateHandler.add(template, "rightType", generate(type.getRight()));
        return template.render();
    }

    private String generateEnumeratedSetElement(EnumeratedSetElementType type) {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", nameHandler.handleIdentifier(type.toString(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        return template.render();
    }

    private String generateDeferredSetElement(DeferredSetElementType type) {
        ST template = group.getInstanceOf("type");
        TemplateHandler.add(template, "type", nameHandler.handleIdentifier(type.toString(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        return template.render();
    }

    /*
    * This function generates code for untyped nodes.
    */
    private String generateUntyped() {
        return group.getInstanceOf("void").render();
    }

    public void setRecordGenerator(RecordGenerator recordGenerator) {
        this.recordGenerator = recordGenerator;
    }
}
