package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class AgentGenerator {
  private static final Class<?> agentClass = RiskItAgent.class;
  private static final String agentPackageLocation = agentClass.getPackageName() + "." + agentClass.getSimpleName();
  private static final String agentEntryLocation = agentPackageLocation.replace('.', '/') + ".class";

  public static void main(String[] args) throws IOException, InterruptedException {
    Process jar = Runtime.getRuntime().exec("gradle jar");
    jar.waitFor();
    System.out.println("Super build finished");
    List<Class<?>> selectionMethods = getInstantiableClasses("at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection");
    List<Class<?>> expansionMethods = getInstantiableClasses("at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion");
    List<Class<?>> simulationMethods = getInstantiableClasses("at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation");
    List<Class<?>> backpropagationMethods = getInstantiableClasses("at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation");

    List<List<Class<?>>> permutations = generatePermutations(List.of(selectionMethods, expansionMethods, simulationMethods, backpropagationMethods));

    permutations.parallelStream()
      .forEach(permutation -> {
        Class<?> selectionMethod = permutation.get(0);
        Class<?> expansionMethod = permutation.get(1);
        Class<?> simulationMethod = permutation.get(2);
        Class<?> backpropagationMethod = permutation.get(3);

        generateModification(args.length > 0 ? args[0] : "build/libs/RiskItForTheBiscuit.jar" , selectionMethod, expansionMethod, simulationMethod, backpropagationMethod);
      });
  }

  private static void generateModification(String input, Class<?> selectionMethod, Class<?> expansionMethod, Class<?> simulationMethod, Class<?> backpropagationMethod) {
    String name = String.format("out/generated/agent_%s_%s_%s_%s", selectionMethod.getSimpleName(), expansionMethod.getSimpleName(), simulationMethod.getSimpleName(), backpropagationMethod.getSimpleName());


    File file = new File(name + ".jar");
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      boolean created = false;
      try {
        created = file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (!created) {
        throw new RuntimeException("Could not create file " + file.getAbsolutePath());
      }
    }
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(new Attributes.Name("Sge-Type"), "agent");
    manifest.getMainAttributes().put(new Attributes.Name("Agent-Class"), agentPackageLocation);
    manifest.getMainAttributes().put(new Attributes.Name("Agent-Name"), file.getName());
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

    System.out.println("Generating " + name + ".jar");

    try (JarInputStream jis = new JarInputStream(new FileInputStream(input));
         FileOutputStream fos = new FileOutputStream(name + ".jar");
         JarOutputStream jos = new JarOutputStream(fos)) {
      jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
      manifest.write(jos);
      jos.closeEntry();
      // Copy entries from the existing JAR to the new JAR
      copyJarEntries(jis, jos, List.of(agentEntryLocation));

      // Modify the specified class in the new JAR
      modifyClass(jos, name, selectionMethod, expansionMethod, simulationMethod, backpropagationMethod);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Finished generating " + name + ".jar");
  }

  private static void modifyClass(JarOutputStream jos, String name, Class<?> selectionMethod, Class<?> expansionMethod, Class<?> simulationMethod, Class<?> backpropagationMethod) throws IOException {
    // Create a new entry for the class
    JarEntry entry = new JarEntry(agentEntryLocation);
    jos.putNextEntry(entry);

    // Write the modified class bytes to the jar file
    jos.write(getModifiedClassBytes(name, selectionMethod, expansionMethod, simulationMethod, backpropagationMethod));

    jos.closeEntry();
  }

  private static byte[] getModifiedClassBytes(String name, Class<?> selectionMethod, Class<?> expansionMethod, Class<?> simulationMethod, Class<?> backpropagationMethod) {
    // Byte Buddy transformation
    DynamicType.Unloaded<?> unloaded = null;
    try {
      unloaded = new ByteBuddy()
        .redefine(agentClass)
        .method(named("getDefaultSelectionStrategy"))
        .intercept(MethodCall.invoke(Instantiator.class.getDeclaredMethod("createInstanceSelection", Class.class)).with(selectionMethod))
        .method(named("getDefaultExpansionStrategy"))
        .intercept(MethodCall.invoke(Instantiator.class.getDeclaredMethod("createInstanceExpansion", Class.class)).with(expansionMethod))
        .method(named("getDefaultSimulationStrategy"))
        .intercept(MethodCall.invoke(Instantiator.class.getDeclaredMethod("createInstanceSimulation", Class.class)).with(simulationMethod))
        .method(named("getDefaultBackpropagationStrategy"))
        .intercept(MethodCall.invoke(Instantiator.class.getDeclaredMethod("createInstanceBackpropagation", Class.class)).with(backpropagationMethod))
        .make();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    return unloaded.getBytes();
  }

  private static Object createInstance(Class<?> clazz) {
    try {
      Constructor<?> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T createInstanceTyped(Class<T> clazz) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void copyJarEntries(JarInputStream jis, JarOutputStream jos, List<String> exclude) throws IOException {
    byte[] buffer = new byte[1024];
    int bytesRead;
    JarEntry entry;

    while ((entry = jis.getNextJarEntry()) != null) {
      if (exclude.contains(entry.getName())) {
        continue;
      }
      jos.putNextEntry(entry);

      while ((bytesRead = jis.read(buffer, 0, buffer.length)) != -1) {
        jos.write(buffer, 0, bytesRead);
      }

      jos.closeEntry();
    }
  }


  private static List<Class<?>> getInstantiableClasses(String packageName) {
    List<Class<?>> instantiableClasses = new ArrayList<>();

    // Replace dots with file separators
    String packagePath = packageName.replace('.', '/');

    // Get the classpath entries
    String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);

    for (String classpathEntry : classpathEntries) {
      File entry = new File(classpathEntry);

      if (entry.isDirectory()) {
        // If it's a directory, scan for classes
        List<Class<?>> classesInPackage = scanClassesInPackage(entry, packagePath, packageName);
        instantiableClasses.addAll(classesInPackage);
      }
    }

    return instantiableClasses;
  }

  private static List<Class<?>> scanClassesInPackage(File directory, String packagePath, String packageName) {
    List<Class<?>> classes = new ArrayList<>();
    File packageDirectory = new File(directory, packagePath);

    if (packageDirectory.exists() && packageDirectory.isDirectory()) {
      File[] files = packageDirectory.listFiles();

      if (files != null) {
        for (File file : files) {
          if (file.isFile() && file.getName().endsWith(".class")) {
            String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);

            try {
              Class<?> clazz = Class.forName(className);
              if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                // Check if the class is instantiable
                classes.add(clazz);
              }
            } catch (ClassNotFoundException e) {
              // Handle exception as needed
              e.printStackTrace();
            }
          }
        }
      }
    }

    return classes;
  }

  public static <T> List<List<T>> generatePermutations(List<List<T>> lists) {
    List<List<T>> result = new ArrayList<>();
    generatePermutationsHelper(lists, 0, new ArrayList<>(), result);
    return result;
  }

  private static <T> void generatePermutationsHelper(List<List<T>> lists, int index, List<T> currentPermutation, List<List<T>> result) {
    if (index == lists.size()) {
      result.add(new ArrayList<>(currentPermutation));
      return;
    }

    List<T> currentList = lists.get(index);

    for (T element : currentList) {
      currentPermutation.add(element);
      generatePermutationsHelper(lists, index + 1, currentPermutation, result);
      currentPermutation.remove(currentPermutation.size() - 1);
    }
  }
}
