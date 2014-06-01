package net.nuttle.commons.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

/**
 * Some unit test demonstrations of Apache Commons' IOUtils.
 * This is for version 1.3.2, currently the most recent version in mvnrepository.
 * But the latest version of IOUtils has many more methods than are tested here.
 */
public class IOUtilsTest 
{
  /**
   * closeQuietly closes IO devices, ignoring any exceptions.
   * This inclues the IO device being null
   */
  @Test
  public void testCloseQuietly() {
    //InputStream
    String data = "A string of data";
    ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
    IOUtils.closeQuietly(is);
    is = null;
    IOUtils.closeQuietly(is);
    
    //Reader
    Reader r = new InputStreamReader(new ByteArrayInputStream(data.getBytes()));
    //Pre-close the reader
    try {
      r.close();
    } catch (IOException ioe) {
      //do nothing
    } finally {
      IOUtils.closeQuietly(r);
    }
    r = null;
    IOUtils.closeQuietly(r);
    
    //OutputStream
    OutputStream os = new ByteArrayOutputStream();
    try {
      os.write(data.getBytes());
    } catch (IOException ioe) {
      //do nothing
    } finally {
      IOUtils.closeQuietly(os);
    }
    
    //Writer
    Writer w = new OutputStreamWriter(new ByteArrayOutputStream());
    try {
      w.write(data);
    } catch (IOException ioe) {
      //do nothing
    } finally {
      IOUtils.closeQuietly(w);
    }
  }
  
  @Test
  public void testContentEquals() throws IOException {
    String data = "ABCD EFGH\nIJK";
    String data2 = "Another string";
    String data3 = "ABCD EFGHIJK";
    //Equal strings
    InputStream is1 = new ByteArrayInputStream(data.getBytes());
    InputStream is2 = new ByteArrayInputStream(data.getBytes());
    assertTrue(IOUtils.contentEquals(is1, is2));
    //Different strings
    is2 = new ByteArrayInputStream(data2.getBytes());
    assertFalse(IOUtils.contentEquals(is1, is2));
    //Equal strings except EOL
    is2 = new ByteArrayInputStream(data3.getBytes());
    assertFalse(IOUtils.contentEquals(is1, is2));
    //Same string, Readers
    Reader r1 = new InputStreamReader(new ByteArrayInputStream(data.getBytes()));
    Reader r2 = new InputStreamReader(new ByteArrayInputStream(data.getBytes()));
    assertTrue(IOUtils.contentEquals(r1, r2));
    //Different strings, Readers
    r2 = new InputStreamReader(new ByteArrayInputStream(data2.getBytes()));
    assertFalse(IOUtils.contentEquals(r1, r2));
    IOUtils.closeQuietly(is1);
    IOUtils.closeQuietly(is2);
    IOUtils.closeQuietly(r1);
    IOUtils.closeQuietly(r2);
  }
  
  @Test
  public void testCopy() throws IOException {
    String data = "ABCDEFGH";
    //InputStream to OutputStream
    InputStream is = new ByteArrayInputStream(data.getBytes());
    OutputStream os = new ByteArrayOutputStream();
    IOUtils.copy(is, os);
    assertEquals(data, os.toString());
    //InputStream to Writer
    Writer w = new StringWriter();
    is.reset();
    IOUtils.copy(is, w);
    assertEquals(data, w.toString());
    //InputStream to Writer, encoding
    w = new StringWriter();
    is.reset();
    IOUtils.copy(is, w, "UTF-8");
    assertEquals(data, w.toString());
    //Reader to OutputStream
    Reader r = new StringReader(data);
    os = new ByteArrayOutputStream();
    IOUtils.copy(r,  os);
    assertEquals(data, os.toString());
    //Reader to Writer
    r.reset();
    w = new StringWriter();
    IOUtils.copy(r,  w);
    assertEquals(data, w.toString());
    //One example of copyLarge, meant for files>2G
    r.reset();
    w = new StringWriter();
    IOUtils.copyLarge(r, w);
    assertEquals(data, w.toString());
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(os);
    IOUtils.closeQuietly(w);
    IOUtils.closeQuietly(r);
  }

  @Test
  public void testLineIterator() throws IOException {
    String data = "abcd\nefgh\rijkl";
    InputStream is = new ByteArrayInputStream(data.getBytes());
    LineIterator lit = IOUtils.lineIterator(is, "UTF-8");
    assertEquals("abcd", lit.nextLine());
    assertEquals("efgh", lit.nextLine());
    assertEquals("ijkl", lit.nextLine());
    assertFalse(lit.hasNext());
    lit.close(); //can also close stream directly or:
    LineIterator.closeQuietly(lit);
  }

  @Test
  public void testReadLines() throws IOException {
    String data = "ABCD\nEFGH\n";
    InputStream is = new ByteArrayInputStream(data.getBytes());
    List<String> lines = IOUtils.readLines(is);
    assertEquals(2, lines.size());
    assertEquals("ABCD", lines.get(0));
    assertEquals("EFGH", lines.get(1));
  }
  
  @Test
  public void testToByteArray() throws IOException {
    String data = "ABCDEFGH";
    //InputStream
    InputStream is = new ByteArrayInputStream(data.getBytes());
    byte[] b = IOUtils.toByteArray(is);
    assertEquals(data, new String(b, "UTF-8"));
    Reader r = new StringReader(data);
    b = IOUtils.toByteArray(is);
    Writer w = new StringWriter();
    IOUtils.copy(r, w);
    assertEquals(data, w.toString());
    //Reader
    StringReader sr = new StringReader(data);
    b = IOUtils.toByteArray(sr, "UTF-8");
    assertEquals(data, new String(b, "UTF-8"));
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(r);
    IOUtils.closeQuietly(w);
    IOUtils.closeQuietly(sr);
    //Untested: URI, URL, URLConnection
  }
  
  @Test 
  public  void testToCharArray() throws IOException {
    //Just test InputStream
    String data = "ABCDEFG";
    InputStream is = new ByteArrayInputStream(data.getBytes());
    char[] c = IOUtils.toCharArray(is);
    assertEquals(data, new String(c));
    IOUtils.closeQuietly(is);
    //Untested: Reader
  }
  
  @Test
  public void testToInputStream() throws IOException {
    String data = "ABCDEFG";
    InputStream is = IOUtils.toInputStream(data);
    StringWriter w = new StringWriter();
    IOUtils.copy(is, w); //kludgy, this is used as *part* of the test, not *what* we are testing
    assertEquals(data, w.toString());
  }

  @Test
  public void testToString() throws IOException {
    String data = "ABCDEFGH";
    //byte[]
    byte[] b = data.getBytes();
    String s = IOUtils.toString(b, "UTF-8");
    assertEquals(data, s);
    //InputStream
    InputStream is = new ByteArrayInputStream(data.getBytes());
    s = IOUtils.toString(is, "UTF-8");
    assertEquals(data, s);
    //Reader
    StringReader r = new StringReader(data);
    s = IOUtils.toString(r);
    assertEquals(data, s);
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(r);
    //Not tested: URI, URL
  }
  
  //To do:  write, writeLines
  
}
