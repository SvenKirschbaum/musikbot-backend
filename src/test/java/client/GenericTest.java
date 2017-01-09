package client;

import static org.junit.Assert.*;

import org.junit.Test;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class GenericTest {

	@Test
	public final void test() {
		assertTrue(0 == 2-2);
	}
	public static void main(String...strings) {
		Argon2 argon2 = Argon2Factory.create();
		String hash = argon2.hash(2, 65536, 1, "abc123");
		System.out.println(hash);
	}

}
