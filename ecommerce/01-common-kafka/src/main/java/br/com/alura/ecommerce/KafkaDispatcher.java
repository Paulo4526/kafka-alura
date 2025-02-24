package br.com.alura.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

//Nosso kafkadispatcher é o responsável por produzir nossas mensagens para o Kafka, ao produzir a mensagem com o KafkaProducer, essa menssagem ficarã disponĩvel para o KafkaConsumer
class KafkaDispatcher<T> implements Closeable {

    //Criando nosso produtor de msg em KAFKA
    private final KafkaProducer<String, T> producer;

    KafkaDispatcher() {
        this.producer = new KafkaProducer<>(properties());
    }

    private static Properties properties() {
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());
        //Configuração abaixo, é responsável pela réplica das mensagens apra outros brokers,, pois caso 1 caia os outros tenha a mesma informação de antes do leader cair.
        //acks=0 -> Significa que nenhuma réplica receberá as informações do leader e caso ele caia, o próximo leader não terá as menssagens de confirmação
        //acks=1 -> Significa que o leader ao cair enviará 1 menssagem para alguma réplica capitar, porém, não espera sincronizar as menssagens, caso algum broker assuma antes da sincronização da mensagem, não será passado nenhuma mensagem pra réplica.
        //ascks=all -> Significa que o leader ao cair enviará a menssagem a todas as réplica, porém, a réplica só assumirá após sincronizar todas as menssagens pendendes.
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // All para esperar todas as mensagens serem sincornizadas com as réplicas.
        return properties;
    }

    //Metodo que fará o envio da mensagem para as classes desejadas no nosso caso Order. Que receberã como parametros topic, string, key e value, sendo o objeto order contendo o valor de cada atributo.
    void send(String topic, String key, T value) throws ExecutionException, InterruptedException {
        var record = new ProducerRecord<>(topic, key, value);
        Callback callback = (data, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            System.out.println("sucesso enviando " + data.topic() + ":::partition " + data.partition() + "/ offset " + data.offset() + "/ timestamp " + data.timestamp());
        };
        producer.send(record, callback).get();
    }

    @Override
    public void close() {
        producer.close();
    }
}
