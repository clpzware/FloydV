package fr.ambient.protection.backend;

import cc.polymorphism.annot.IncludeReference;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@IncludeReference
public class EncryptionPair {
    private String nextServerKey = Base64.getEncoder().encodeToString("1a55671b71956a7b31bee79e6a34a7812536f879ddb78a2f34b43865931bc177".getBytes(StandardCharsets.UTF_8)), nextClientKey = Base64.getEncoder().encodeToString("1a55671b71956a7b31bee79e6a34a7812536f879ddb78a2f34b43865931bc177".getBytes(StandardCharsets.UTF_8));
}
