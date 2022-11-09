/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.SneakyThrows;

/**
 * @author Andreas Penski
 */
public class DateFactory {

    private DateFactory() {
        // hide
    }

    @SneakyThrows
    public static XMLGregorianCalendar createTimestamp() {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

    }
}
