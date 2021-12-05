/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 31.03.2020
 */
@Getter
@Setter
public class Recipient {

    // size:2 = combined size of followed fields (w)
    private int size;
    // size:p - hash of Public Key
    private byte[] hash;
    // size:(w - p) - Simple Key Blob
    private byte[] simpleKeyBlob;
}
