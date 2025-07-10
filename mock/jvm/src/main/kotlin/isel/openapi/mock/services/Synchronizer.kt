package isel.openapi.mock.services
//
//import jakarta.annotation.PostConstruct
//import jakarta.annotation.PreDestroy
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.DisposableBean
//import org.springframework.stereotype.Component
//import java.util.concurrent.locks.ReentrantLock
//import kotlin.concurrent.withLock
//
///**
// * 2 Threads.
// * 1 - chama queue quando recebe notificaçao da DB. Coloca a flag update a true para indicar que é para atualizar a informaçao em memoria(criar uma nova instancia). Sinaliza
// * a outra thread, se ela nao estiver à espera na condiçao, nao há problema.
// * 2 - esta Thread simplesmente chama dequeue e fica no seu ciclo. Enquanto a flag update estiver a falso, coloca-se à espera. Quando sair da espera e a flag estiver a true,
// * coloca-a a falso, larga o lock e faz a atualizaçao do Router em memoria com a info da DB. Fica num ciclo a fazer isto.
// */
//@Component
//class  Synchronizer(
//    private val services: DynamicHandlerServices
//)  {
//
//    private val lock = ReentrantLock()
//
//    private val condition = lock.newCondition()
//
//    private var update = false
//
//    private lateinit var thread: Thread
//
//    fun queue() {
//        lock.withLock {
//            update = true
//            condition.signal()
//        }
//
//    }
//
//    @PostConstruct
//    fun dequeue() {
//        thread = Thread.ofPlatform().start {
//            while (true) {
//                try {
//                    lock.withLock {
//                        while (!update) {
//                            condition.await()
//                        }
//                        update = false
//                    }
//                    services.updateDynamicRouter()
//                } catch (e: InterruptedException) {
//                    logger.info("Thread interrupted, stopping synchronizer.")
//                    Thread.currentThread().interrupt()
//                    break
//                }
//            }
//        }
//    }
//
//    @PreDestroy
//    fun destroy() {
//        logger.info("Stopping synchronizer thread.")
//        thread.interrupt()
//        logger.info("Waiting for synchronizer thread to finish.")
//        thread.join()
//        logger.info("Synchronizer thread stopped.")
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger("Synchronizer")
//    }
//}