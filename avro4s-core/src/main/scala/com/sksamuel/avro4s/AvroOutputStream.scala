package com.sksamuel.avro4s

import java.io.{File, OutputStream}
import java.nio.file.{Files, Path}

import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import shapeless.Lazy

class AvroOutputStream[T](os: OutputStream)(implicit schema: Lazy[ToSchema[T]], writer: Lazy[AvroWriter[T]]) {

  val datumWriter = new GenericDatumWriter[GenericRecord]()
  val dataFileWriter = new DataFileWriter[GenericRecord](datumWriter)
  dataFileWriter.create(schema.value.apply, os)

  def write(ts: Seq[T]): Unit = ts.foreach(write)

  def write(t: T): Unit = {
    val record = writer.value(t)
    dataFileWriter.append(record)
  }

  def flush(): Unit = dataFileWriter.flush()

  def fSync(): Unit = dataFileWriter.fSync()

  def close(): Unit = {
    dataFileWriter.flush()
    dataFileWriter.close()
  }
}

object AvroOutputStream {
  def apply[T](file: File)(implicit schema: Lazy[ToSchema[T]], writer: Lazy[AvroWriter[T]]): AvroOutputStream[T] = apply(file.toPath)
  def apply[T](path: Path)(implicit schema: Lazy[ToSchema[T]], writer: Lazy[AvroWriter[T]]): AvroOutputStream[T] = apply(Files.newOutputStream(path))
  def apply[T](os: OutputStream)(implicit schema: Lazy[ToSchema[T]], writer: Lazy[AvroWriter[T]]): AvroOutputStream[T] = new AvroOutputStream[T](os)
}