/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.ingestion.sink.cassandra;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.db.marshal.DateType;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import static org.fest.assertions.Assertions.*;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.core.DataType;

public class EventParserTest {

	private EventParser instance;

	@Before
	public void before() throws IOException {
		this.instance = new EventParser(IOUtils.toString(this.getClass()
				.getResourceAsStream("/definition.json")));
	}

	@Test
	public void shouldParseDate() throws Exception {
        DateTime now = DateTime.now();
		assertThat(EventParser.parseDate(ISODateTimeFormat.dateTime().print(now), null))
                .isEqualTo(now.toDate());
        assertThat(EventParser.parseDate(ISODateTimeFormat.dateTimeNoMillis().print(now), null))
                .isEqualTo(now.withMillisOfSecond(0).toDate());
        assertThat(EventParser.parseDate(Long.toString(now.getMillis()), null))
                .isEqualTo(now.toDate());
        assertThat(EventParser.parseDate("10/12/2004 12:01:02", "dd/MM/yyyy HH:mm:ss"))
                .isEqualTo(new DateTime(2004, 12, 10, 12, 1, 2, 0).toDate());
	}

	@Test
	public void shoudParsePrimitiveTypes() throws Exception {
		Object integer = EventParser.parseValue("1", DataType.Name.INT, "");
        assertThat(integer)
                .isInstanceOf(Integer.class)
                .isEqualTo(1);

        Object _float = EventParser.parseValue("1", DataType.Name.FLOAT, "");
        assertThat(_float)
                .isInstanceOf(Float.class)
                .isEqualTo(1f);
        _float = EventParser.parseValue("1.0", DataType.Name.FLOAT, "");
        assertThat(_float)
                .isInstanceOf(Float.class)
                .isEqualTo(1f);
        _float = EventParser.parseValue(" 1 . 0 ", DataType.Name.FLOAT, "");
        assertThat(_float)
                .isInstanceOf(Float.class)
                .isEqualTo(1f);

        Object _double = EventParser.parseValue("1", DataType.Name.DOUBLE, "");
        assertThat(_double)
                .isInstanceOf(Double.class)
                .isEqualTo(1.0);
        _double = EventParser.parseValue("1.0", DataType.Name.DOUBLE, "");
        assertThat(_double)
                .isInstanceOf(Double.class)
                .isEqualTo(1.0);
        _double = EventParser.parseValue(" 1 . 0 ", DataType.Name.DOUBLE, "");
        assertThat(_double)
                .isInstanceOf(Double.class)
                .isEqualTo(1.0);

        Object string = EventParser.parseValue("string", DataType.Name.TEXT, "");
        assertThat(string)
                .isInstanceOf(String.class)
                .isEqualTo("string");

		Object bool = EventParser.parseValue("true", DataType.Name.BOOLEAN, "");
        assertThat(bool)
                .isInstanceOf(Boolean.class)
                .isEqualTo(true);

        Object addr = EventParser.parseValue("192.168.1.1", DataType.Name.INET, "");
        assertThat(addr)
                .isInstanceOf(InetAddress.class)
                .isEqualTo(InetAddress.getByName("192.168.1.1"));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void shouldParseLists() throws Exception {
		String rawList = "1;2;3";
		CassandraField<List> listField = EventParser.parseList(rawList,
				this.instance.getDefinition().getFields().get(1));
		Assert.assertEquals(listField.getValue().get(0), new Integer(1));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void shouldParseMap() throws Exception {
		String rawMap = "1:192.168.1.1;2:192.168.1.2;3:192.168.1.3";
		CassandraField<Map> mapField = EventParser.parseMap(rawMap,
				this.instance.getDefinition().getFields().get(2));
		Map<Integer, InetAddress> expected = new HashMap<Integer, InetAddress>();
		expected.put(1, InetAddress.getByName("192.168.1.1"));
		Assert.assertEquals(expected.get(1), mapField.getValue().get(1));
	}

}
