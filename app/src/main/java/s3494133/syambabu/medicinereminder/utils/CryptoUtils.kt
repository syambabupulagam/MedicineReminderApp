package s3494133.syambabu.medicinereminder.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "YourSuperSecretKeyForEncryptionA"
    private const val IV = "YourSuperSecretIvA"

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(KEY.toByteArray().copyOf(32), "AES")
        val ivSpec = IvParameterSpec(IV.toByteArray().copyOf(16))
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(value: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(KEY.toByteArray().copyOf(32), "AES")
        val ivSpec = IvParameterSpec(IV.toByteArray().copyOf(16))
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val decoded = Base64.decode(value, Base64.DEFAULT)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
}