package isel.openapi.mock

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 2 Threads.
 * 1 - chama queue quando recebe notificaçao da DB. Coloca a flag update a true para indicar que é para atualizar a informaçao em memoria(criar uma nova instancia). Sinaliza
 * a outra thread, se ela nao estiver à espera na condiçao, nao há problema.
 * 2 - esta Thread simplesmente chama dequeue e fica no seu ciclo. Enquanto a flag update estiver a falso, coloca-se à espera. Quando sair da espera e a flag estiver a true,
 * coloca-a a falso, larga o lock e faz a atualizaçao do Router em memoria com a info da DB. Fica num ciclo a fazer isto.
 */
class Synchronizer {

    private val lock = ReentrantLock()

    private val condition = lock.newCondition()

    private var update = false

    fun queue() {
        lock.withLock {
            update = true
            condition.signal()
        }

    }

    fun dequeue() {
        while (true) {
            lock.withLock {
                while (!update) {
                    condition.await()
                }
                update = false
            }

            //TODO ir buscar os dados à DB e criar nova instancia de Router
        }

    }
}