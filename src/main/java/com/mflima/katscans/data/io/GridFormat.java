package com.mflima.katscans.data.io;

import com.mflima.katscans.data.VoxelMatrix;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/** @author Marcelo Lima */
class GridFormat implements LoadSaveFormat {

  private static final int FORMAT_MAX_VALUE = 65536;

  private static final FileFilter FILE_FILTER =
      new FileNameExtensionFilter("Grid volume data", "grid");

  @Override
  public String getName() {
    return "Grid";
  }

  @Override
  public char getMnemonic() {
    return 'G';
  }

  @Override
  public FileFilter getFileFilter() {
    return FILE_FILTER;
  }

  @Override
  public int getMaxValue() {
    return FORMAT_MAX_VALUE;
  }

  @Override
  public FormatHeader getHeader(InputStream stream) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES * 4 + Double.BYTES * 3);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    int sizeZ, sizeY, sizeX;
    double ratioX, ratioY, ratioZ;
    int max;

    if (stream.read(byteBuffer.array()) > 0) {
      sizeX = byteBuffer.getShort();
      sizeY = byteBuffer.getShort();
      sizeZ = byteBuffer.getShort();
      ratioX = byteBuffer.getDouble();
      ratioY = byteBuffer.getDouble();
      ratioZ = byteBuffer.getDouble();
      max = byteBuffer.getShort();
    } else {
      throw new StreamCorruptedException("Could not read dat header from the stream");
    }

    double ratioBase = Math.min(ratioX, Math.min(ratioY, ratioZ));
    ratioX /= ratioBase;
    ratioY /= ratioBase;
    ratioZ /= ratioBase;

    return new FormatHeader(sizeX, sizeY, sizeZ, ratioX, ratioY, ratioZ, 0, max);
  }

  @Override
  public VoxelMatrix loadData(InputStream stream, LoadSaveOptions options) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES * 4 + Double.BYTES * 3);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    int sizeY, sizeX;
    int optionSizeX = options.getSizeX();
    int optionSizeY = options.getSizeY();
    int optionSizeZ = options.getSizeZ();

    if (stream.read(byteBuffer.array()) > 0) {
      sizeX = byteBuffer.getShort();
      sizeY = byteBuffer.getShort();
    } else {
      throw new StreamCorruptedException("Could not read dat header from the stream");
    }

    VoxelMatrix matrix = new VoxelMatrix(options);

    ShortBuffer shortBuffer;
    byteBuffer = ByteBuffer.allocate(sizeX * 2);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    short[] grid = matrix.getData();

    for (int z = 0; z < optionSizeZ; z++) {
      for (int y = 0; y < sizeY; y++) {
        if (stream.read(byteBuffer.array()) < sizeX * 2) {
          throw new IOException("Expected data, but could not be read");
        }

        if (y >= optionSizeY) {
          continue;
        }

        shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.get(
            grid,
            z * optionSizeY * optionSizeX + ((optionSizeY - 1) - y) * optionSizeX,
            optionSizeX);
      }
    }

    matrix.initialize();
    return matrix;
  }

  @Override
  public void saveData(OutputStream stream, VoxelMatrix object) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
