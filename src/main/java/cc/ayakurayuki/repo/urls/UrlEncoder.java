// MIT License
//
// Copyright (c) 2023 Joshua Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package cc.ayakurayuki.repo.urls;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A URI encoding class that follows RFC 3986
 * <p>
 * Source: <a href="https://github.com/senojj/uriencode">senojj/uriencode: A URI encoding class that follows RFC 3986</a>
 *
 * @author senojj (modified by AyakuraYuki)
 */
public abstract class UrlEncoder {

  static final char[] upperhex = "0123456789ABCDEF".toCharArray();

  static BitSet unreservedCharacters;

  static {
    unreservedCharacters = new BitSet(256);

    int i;
    for (i = 'a'; i <= 'z'; i++) {
      unreservedCharacters.set(i);
    }
    for (i = 'A'; i <= 'Z'; i++) {
      unreservedCharacters.set(i);
    }
    for (i = '0'; i <= '9'; i++) {
      unreservedCharacters.set(i);
    }

    unreservedCharacters.set('-');
    unreservedCharacters.set('_');
    unreservedCharacters.set('.');
    unreservedCharacters.set('*');
    unreservedCharacters.set('~');
  }

  static void toHex(StringBuilder out, byte b) {
    char c = upperhex[(b >> 4) & 0xF];
    out.append(c);
    c = upperhex[b & 0xF];
    out.append(c);
  }

  @Nonnull
  public static String encode(@Nonnull String s) {
    return encode(s, StandardCharsets.UTF_8);
  }

  @Nonnull
  public static String encode(@Nonnull String s, @Nullable Charset charset) {
    if (charset == null) {
      charset = StandardCharsets.UTF_8;
    }

    StringBuilder out = new StringBuilder(s.length());
    CharArrayWriter charArrayWriter = new CharArrayWriter();

    for (int i = 0; i < s.length(); ) {
      int c = s.charAt(i);

      if (unreservedCharacters.get(c)) {
        out.append((char) c);
        i++;
        continue;
      }

      do {
        charArrayWriter.write(c);
        i++;
      } while (i < s.length() && !unreservedCharacters.get((c = s.charAt(i))));

      charArrayWriter.flush();
      String string = charArrayWriter.toString();
      byte[] bytes = string.getBytes(charset);
      for (byte b : bytes) {
        out.append('%');
        toHex(out, b);
      }
      charArrayWriter.reset();
    }

    return out.toString();
  }

}
