package at.ac.tuwien.ifs.sge.agent.risk.util;

import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import org.nd4j.linalg.api.ndarray.INDArray;

public class DatasetWriter {
  public void appendToHDF_legacy(INDArray indArray, float... values) {
    IHDF5Writer writer = HDF5Factory.open("data.h5");

    float[] indArrayData = indArray.data().asFloat();

    float[] combinedData = new float[indArrayData.length + values.length];
    System.arraycopy(indArrayData, 0, combinedData, 0, indArrayData.length);
    System.arraycopy(values, 0, combinedData, indArrayData.length, values.length);

    long[] currentDims = writer.object().getDimensions("/dataset");
    long[] newDims = {currentDims[0] + 1, combinedData.length};

    /*
    not working:
    // Create a new dataset with the new dimensions
    writer.float32().createMDArray("/dataset", newDims);

    // Write the combined data to the new dataset
    writer.float32().writeMDArray("/dataset", new MDFloatArray(combinedData, newDims));

    long[] currentDims = writer.float32().getArraySize("/dataset");
    long[] newDims = {currentDims[0] + 1, combinedData.length};

    writer.float32().setExtendable("/dataset", 1, combinedData.length);
    writer.float32().extend("/dataset", newDims);
    writer.float32().writeArrayBlockWithOffset("/dataset", combinedData, 1, currentDims[0]);
    */

    writer.close();
  }

  public static void appendToHDF(String filePath, INDArray gameState, float scalarValue) {
    // Open the HDF5 file
    IHDF5Writer writer = HDF5Factory.open(filePath);

    // Get the current dimensions of the dataset
    long[] currentDims = writer.object().getDimensions("/dataset");

    // Create a new dimension array where the first dimension is incremented by 1
    int[] newDims = {(int) currentDims[0] + 1, (int) gameState.length() + 1};

    // Create a new dataset with the new dimensions
    writer.float32().createMDArray("/dataset", newDims);

    // Convert the game state to a 1D array
    float[] gameStateArray = gameState.data().asFloat();

    // Create a combined array with the game state and scalar value
    float[] combinedData = new float[gameStateArray.length + 1];
    System.arraycopy(gameStateArray, 0, combinedData, 0, gameStateArray.length);
    combinedData[combinedData.length - 1] = scalarValue;

    // Write the combined data to the new dataset
    writer.float32().writeMDArray("/dataset", new MDFloatArray(combinedData, newDims));

    // Close the HDF5 file
    writer.close();
  }

  public static void appendToHDFArray(String filePath, INDArray gameState, float... values) {
    // Open the HDF5 file
    IHDF5Writer writer = HDF5Factory.open(filePath);

    // Get the current dimensions of the dataset
    long[] currentDims = writer.object().getDimensions("/dataset");

    // Create a new dimension array where the first dimension is incremented by 1
    // and the second dimension is the sum of the game state length and the values length
    int[] newDims = {(int) currentDims[0] + 1, (int) gameState.length() + values.length};

    // Create a new dataset with the new dimensions
    writer.float32().createMDArray("/dataset", newDims);

    // Convert the game state to a 1D array
    float[] gameStateArray = gameState.data().asFloat();

    // Create a combined array with the game state and scalar values
    float[] combinedData = new float[gameStateArray.length + values.length];
    System.arraycopy(gameStateArray, 0, combinedData, 0, gameStateArray.length);
    System.arraycopy(values, 0, combinedData, gameStateArray.length, values.length);

    // Write the combined data to the new dataset
    writer.float32().writeMDArray("/dataset", new MDFloatArray(combinedData, newDims));

    // Close the HDF5 file
    writer.close();
  }
}
