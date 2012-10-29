package proxy.transform;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTransform {
	AnonimousTransformer a;
	public TestTransform(){
		a = new AnonimousTransformer();
		}

	@Test
	public void test() {
		assertEquals("From: Anonymous <anon@anon.org>", a.transform("From: ASdAsd <Korv@lol.com>"));
		assertEquals("From: Anonymous <anon@anon.org>", a.transform("From: Karl Petter <bla@lol.com>"));
	}

}
