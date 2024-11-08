package de.hhu.stups.codegenerator.ast;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.parser.antlr.BProject;
import de.prob.parser.antlr.ScopeException;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.MachineReferenceNode;
import de.prob.parser.ast.visitors.MachineScopeChecker;
import de.prob.parser.ast.visitors.TypeChecker;
import de.prob.parser.ast.visitors.TypeErrorException;
import de.prob.parser.util.Utils;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SableCCBParser {

    public static BProject createBProjectFromMainMachineFile(File mainBFile, boolean typecheck, boolean scopecheck) throws IOException, TypeErrorException, ScopeException, BCompoundException {
        final File parentFolder = mainBFile.getParentFile();
        final List<MachineNode> machines = new ArrayList<>();
        final Start mainMachineCST = parse(mainBFile);


        VisitorCoordinator coordinator = new VisitorCoordinator();
        final MachineNode main = coordinator.convertMachineNode(mainMachineCST);

        checkMachineName(mainBFile, main.getName());

        machines.add(main);
        final Set<String> parsedMachines = new HashSet<>();
        parsedMachines.add(main.getName());
        final List<MachineReferenceNode> todo = new ArrayList<>();
        todo.addAll(main.getMachineReferences());
        while (!todo.isEmpty()) {
            final MachineReferenceNode next = todo.iterator().next();
            todo.remove(next);
            final String name = next.getMachineName();
            if (!parsedMachines.contains(name)) {
                final File file = getFile(parentFolder, name);
                //checkMachineName(file, name);
                final Start cst = parse(file);
                final MachineNode ast = coordinator.convertMachineNode(cst);
                ast.setPrefix(next.getPrefix());
                machines.add(ast);
                for (MachineReferenceNode machineReferenceNode : ast.getMachineReferences()) {
                    final String refName = machineReferenceNode.getMachineName();
                    if (!parsedMachines.contains(refName)) {
                        todo.add(machineReferenceNode);
                    }
                }
            }
        }
        return createBProject(machines, typecheck, scopecheck);
    }

    public static BProject createBProjectFromMainMachineFile(File mainBFile) throws TypeErrorException, ScopeException, IOException, BCompoundException {
        return createBProjectFromMainMachineFile(mainBFile, true, true);
    }

    public static Start parse(File bFile) throws IOException, BCompoundException {
        BParser parser = new BParser();

//        FileInputStream fileInputStream = new FileInputStream(bFile);
//        CharStream charStream = CharStreams.fromStream(fileInputStream);
        return parser.parseFile(bFile);
    }

    public static Start parse(final CharStream charStream) throws BCompoundException {
        BParser parser = new BParser();
        System.out.println(String.valueOf(charStream));
        return parser.parseFile(new File(String.valueOf(charStream)));
    }

    protected static void checkMachineName(File file, String name) {
        if(!file.exists()) {
            throw new RuntimeException(String.format("Machine %s must have the same name as its file", name));
        }
        String path = file.getName().replaceAll(".mch", "");
        if(!path.equals(name)) {
            throw new RuntimeException(String.format("Machine %s must have the same name as its file", name));
        }
    }

    protected static void determineMachineDependencies(final MachineNode machineNode,
                                                       final Map<String, MachineNode> machineNodes, final Map<String, Set<String>> dependencies,
                                                       final List<String> ancestors) {
        final String name = machineNode.toString();
        ancestors.add(name);

        final Set<String> set = new HashSet<>();
        for (MachineReferenceNode machineReferenceNode : machineNode.getMachineReferences()) {
            final String refName = machineReferenceNode.toString();
            if (ancestors.contains(refName)) {
                throw new RuntimeException("Cycle detected");
            }
            set.add(refName);
            final MachineNode refMachineNode = machineNodes.get(refName);
            machineReferenceNode.setMachineNode(refMachineNode);
            determineMachineDependencies(refMachineNode, machineNodes, dependencies, new ArrayList<>(ancestors));
            set.addAll(dependencies.get(refName));
        }
        dependencies.put(name, set);

    }

    protected static void sortMachineNodes(List<MachineNode> machineNodeList) {
        final Map<String, MachineNode> machineNodeMap = new HashMap<>();
        for (MachineNode machineNode : machineNodeList) {
            machineNodeMap.put(machineNode.toString(), machineNode);
        }
        Map<String, Set<String>> dependencies = new HashMap<>();
        determineMachineDependencies(machineNodeList.get(0), machineNodeMap, dependencies, new ArrayList<>());
        List<String> machineNameList = Utils.sortByTopologicalOrder(dependencies);
        machineNodeList.clear();
        for (String machineName : machineNameList) {
            machineNodeList.add(machineNodeMap.get(machineName));
        }
    }

    public static BProject createBProject(List<MachineNode> machineNodeList, boolean typecheck, boolean scopecheck) throws TypeErrorException, ScopeException {
        // determine machine order

        sortMachineNodes(machineNodeList);
        for (int i = machineNodeList.size() - 1; i >= 0; i--) {
            MachineNode machineNode = machineNodeList.get(i);
            if(scopecheck) {
                new MachineScopeChecker(machineNode);
            }
        }
        if(typecheck) {
            for (int i = machineNodeList.size() - 1; i >= 0; i--) {
                MachineNode machineNode = machineNodeList.get(i);
                new TypeChecker(machineNode);
            }
        }
        return new BProject(machineNodeList);
    }

    protected static File getFile(File parentFolder, String name) {
        // TODO try different file name extensions
        return new File(parentFolder, name + ".mch");
    }
}
