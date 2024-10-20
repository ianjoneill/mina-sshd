/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sshd.common.util.security.eddsa.bouncycastle;

import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder;
import org.apache.sshd.common.config.keys.PublicKeyEntryDecoder;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport;
import org.apache.sshd.common.util.security.eddsa.generic.GenericSignatureEd25519;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jcajce.interfaces.EdDSAKey;
import org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.spec.RawEncodedKeySpec;

public class BouncyCastleEdDSASupport implements EdDSASupport<EdDSAPublicKey, EdDSAPrivateKey> {

    public BouncyCastleEdDSASupport() {
    }

    @Override
    public PublicKeyEntryDecoder<EdDSAPublicKey, EdDSAPrivateKey> getEDDSAPublicKeyEntryDecoder() {
        return BouncyCastleEd25519PublicKeyDecoder.INSTANCE;
    }

    @Override
    public PrivateKeyEntryDecoder<EdDSAPublicKey, EdDSAPrivateKey> getOpenSSHEDDSAPrivateKeyEntryDecoder() {
        return BouncyCastleOpenSSHEd25519PrivateKeyEntryDecoder.INSTANCE;
    }

    @Override
    public Signature getEDDSASigner() {
        return new GenericSignatureEd25519(SecurityUtils.EDDSA);
    }

    @Override
    public int getEDDSAKeySize(Key key) {
        return key instanceof EdDSAKey ? KEY_SIZE : -1;
    }

    @Override
    public Class<? extends PublicKey> getEDDSAPublicKeyType() {
        return EdDSAPublicKey.class;
    }

    @Override
    public Class<? extends PrivateKey> getEDDSAPrivateKeyType() {
        return EdDSAPrivateKey.class;
    }

    @Override
    public boolean compareEDDSAPPublicKeys(PublicKey k1, PublicKey k2) {
        if (!(k1 instanceof EdDSAPublicKey) || !(k2 instanceof EdDSAPublicKey)) {
            return false;
        }

        return k1.equals(k2);
    }

    @Override
    public boolean compareEDDSAPrivateKeys(PrivateKey k1, PrivateKey k2) {
        if (!(k1 instanceof EdDSAPrivateKey) || !(k2 instanceof EdDSAPrivateKey)) {
            return false;
        }

        return k1.equals(k2);
    }

    @Override
    public PublicKey recoverEDDSAPublicKey(PrivateKey key) throws GeneralSecurityException {
        if (!(key instanceof EdDSAPrivateKey)) {
            throw new InvalidKeyException("Private key is not " + SecurityUtils.EDDSA);
        }
        EdDSAPrivateKey edDSAKey = (EdDSAPrivateKey) key;
        return edDSAKey.getPublicKey();
    }

    @Override
    public PublicKey generateEDDSAPublicKey(byte[] seed) throws GeneralSecurityException {
        RawEncodedKeySpec keySpec = new RawEncodedKeySpec(seed);
        KeyFactory factory = SecurityUtils.getKeyFactory("Ed25519");
        return factory.generatePublic(keySpec);
    }

    @Override
    public PrivateKey generateEDDSAPrivateKey(byte[] seed) throws GeneralSecurityException, IOException {
        Ed25519PrivateKeyParameters parameters = new Ed25519PrivateKeyParameters(seed);
        PrivateKeyInfo info = PrivateKeyInfoFactory.createPrivateKeyInfo(parameters);
        KeyFactory factory = SecurityUtils.getKeyFactory("Ed25519");
        return factory.generatePrivate(new PKCS8EncodedKeySpec(info.getEncoded()));
    }

    @Override
    public <B extends Buffer> B putRawEDDSAPublicKey(B buffer, PublicKey key) {
        EdDSAPublicKey edKey = ValidateUtils.checkInstanceOf(key, EdDSAPublicKey.class, "Not an EDDSA public key: %s", key);
        buffer.putBytes(edKey.getPointEncoding());
        return buffer;
    }

    @Override
    public <B extends Buffer> B putEDDSAKeyPair(B buffer, PublicKey pubKey, PrivateKey prvKey) {
        ValidateUtils.checkInstanceOf(pubKey, EdDSAPublicKey.class, "Not an EDDSA public key: %s", pubKey);
        ValidateUtils.checkInstanceOf(prvKey, EdDSAPrivateKey.class, "Not an EDDSA private key: %s", prvKey);
        throw new UnsupportedOperationException("Full SSHD-440 implementation N/A");
    }
}
