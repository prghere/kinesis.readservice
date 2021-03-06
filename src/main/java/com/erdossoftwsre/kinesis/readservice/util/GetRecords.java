package com.erdossoftwsre.kinesis.readservice.util;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GetRecords {

    public static void main(String[] args) {

        final String USAGE = "\n" +
                "Usage:\n" +
                "    GetRecords <streamName>\n\n" +
                "Where:\n" +
                "    streamName - The Amazon Kinesis data stream to read from (for example, StockTradeStream).\n\n" +
                "Example:\n" +
                "    GetRecords streamName\n";

        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String streamName = args[0];
        Region region = Region.US_EAST_1;
        KinesisClient kinesisClient = KinesisClient.builder()
                .region(region)
                .build();

        printData(kinesisClient,streamName);
        kinesisClient.close();
    }

    public static void printData(KinesisClient kinesisClient, String streamName) {

        String shardIterator;
        String lastShardId = null;

        // Retrieve the Shards from a Stream
        DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
                .streamName(streamName)
                .build();
        List<Shard> shards = new ArrayList<>();

        DescribeStreamResponse streamRes;
        do {
            streamRes = kinesisClient.describeStream(describeStreamRequest);
            shards.addAll(streamRes.streamDescription().shards());

            if (shards.size() > 0) {
                lastShardId = shards.get(shards.size() - 1).shardId();
            }
        } while (streamRes.streamDescription().hasMoreShards());

        GetShardIteratorRequest itReq = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardIteratorType("TRIM_HORIZON")
                .shardId(shards.get(0).shardId())
                .build();

        GetShardIteratorResponse shardIteratorResult = kinesisClient.getShardIterator(itReq);
        shardIterator = shardIteratorResult.shardIterator();

        // Continuously read data records from shard.
        List<Record> records;

        // Create new GetRecordsRequest with existing shardIterator.
        // Set maximum records to return to 1000.
        GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                .shardIterator(shardIterator)
                .limit(1000)
                .build();

        GetRecordsResponse result = kinesisClient.getRecords(recordsRequest);

        // Put result into record list. Result may be empty.
        records = result.records();

        // Print records
        for (Record record : records) {
            SdkBytes byteBuffer = record.data();
            System.out.println(String.format("Seq No: %s - %s", record.sequenceNumber(),
                    new String(byteBuffer.asByteArray())));
        }
    }

    public static String readStreamData(
            String streamName,
            String region
    ){
        try(KinesisClient kinesisClient = KinesisClient.builder()
                .region(Region.of(region))
                .build())
        {
            List<Shard> shards = new ArrayList<>();
            String lastShardId = "";

            DescribeStreamResponse streamRes;
            do {
                streamRes = kinesisClient.describeStream(
                        DescribeStreamRequest.builder()
                        .streamName(streamName)
                        .build()
                );

                shards.addAll(streamRes.streamDescription().shards());

                if (shards.size() > 0) {
                    lastShardId = shards.get(shards.size() - 1).shardId();
                }
            } while (streamRes.streamDescription().hasMoreShards());

            GetShardIteratorRequest itReq = GetShardIteratorRequest.builder()
                    .streamName(streamName)
                    .shardIteratorType("TRIM_HORIZON")
                    .shardId(shards.get(0).shardId())
                    .build();

            GetShardIteratorResponse shardIteratorResult = kinesisClient.getShardIterator(itReq);
            String shardIterator = shardIteratorResult.shardIterator();

            // Continuously read data records from shard.
            List<Record> records;

            // Create new GetRecordsRequest with existing shardIterator.
            // Set maximum records to return to 1000.
            GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                    .shardIterator(shardIterator)
                    .limit(1000)
                    .build();

            GetRecordsResponse result = kinesisClient.getRecords(recordsRequest);

            // Put result into record list. Result may be empty.
            records = result.records();

            // Print records
            for (Record record : records) {
                SdkBytes byteBuffer = record.data();
                System.out.println(String.format("Seq No: %s - %s", record.sequenceNumber(),
                        new String(byteBuffer.asByteArray())));
            }

        }
        return "string";
    }
}

