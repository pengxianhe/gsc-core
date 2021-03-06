package org.gsc.core.wrapper;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.gsc.common.exception.BadItemException;
import org.gsc.common.exception.ValidateSignatureException;
import org.gsc.common.utils.MerkleTree;
import org.gsc.common.utils.Sha256Hash;
import org.gsc.crypto.ECKey;
import org.gsc.crypto.ECKey.ECDSASignature;
import org.gsc.protos.Protocol.Block;
import org.gsc.protos.Protocol.BlockHeader;
import org.gsc.protos.Protocol.Transaction;

public class BlockWrapper extends org.gsc.core.chain.BlockHeader implements StoreWrapper<Block>{

  @Override
  public Block getInstance() {
    return block;
  }

  private Block block;

  public boolean generatedByMyself = false;

  private List<TransactionWrapper> transactions;

  public BlockWrapper(long timestamp, Sha256Hash parentHash, long number,  ByteString producerAddress,
      List<Transaction> transactionList) {
    super(timestamp, parentHash, number, producerAddress);

    Block.Builder blockBuild = Block.newBuilder();
    transactionList.forEach(trx -> blockBuild.addTransactions(trx));
    this.block = blockBuild.setBlockHeader(blockHeader).build();
    initTxs();
  }

  public BlockWrapper(long timestamp, Sha256Hash parentHash, long number,  ByteString producerAddress) {
    super(timestamp, parentHash, number, producerAddress);
    initTxs();
  }

  public void addTransaction(TransactionWrapper pendingTrx) {
    this.block = this.block.toBuilder().addTransactions(pendingTrx.getInstance()).build();
    getTransactions().add(pendingTrx);
  }

  public List<TransactionWrapper> getTransactions() {
    return transactions;
  }

  private void initTxs() {
    transactions = this.block.getTransactionsList().stream()
        .map(trx -> new TransactionWrapper(trx))
        .collect(Collectors.toList());
  }


  public void sign(byte[] privateKey) {
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    ECDSASignature signature = ecKey.sign(getRawHash().getBytes());
    ByteString sig = ByteString.copyFrom(signature.toByteArray());

    BlockHeader blockHeader = this.block.getBlockHeader().toBuilder().setWitnessSignature(sig)
        .build();

    this.block = this.block.toBuilder().setBlockHeader(blockHeader).build();
  }

  public Sha256Hash calcMerkleRoot() {
    List<Transaction> transactionsList = this.block.getTransactionsList();

    if (CollectionUtils.isEmpty(transactionsList)) {
      return Sha256Hash.ZERO_HASH;
    }

    Vector<Sha256Hash> ids = transactionsList.stream()
        .map(TransactionWrapper::new)
        .map(TransactionWrapper::getMerkleHash)
        .collect(Collectors.toCollection(Vector::new));

    return MerkleTree.getInstance().createTree(ids).getRoot().getHash();
  }

  public boolean validateSignature() throws ValidateSignatureException {
    try {
      return Arrays
          .equals(ECKey.signatureToAddress(getRawHash().getBytes(),
              ECKey.getBase64FromByteString(block.getBlockHeader().getWitnessSignature())),
              block.getBlockHeader().getRawData().getProducerAddress().toByteArray());
    } catch (SignatureException e) {
      throw new ValidateSignatureException(e.getMessage());
    }
  }

  public void setMerkleRoot() {
    List<Transaction> transactionsList = this.block.getTransactionsList();

    Vector<Sha256Hash> ids = transactionsList.stream()
        .map(TransactionWrapper::new)
        .map(TransactionWrapper::getMerkleHash)
        .collect(Collectors.toCollection(Vector::new));

    BlockHeader.raw blockHeaderRaw =
        this.block.getBlockHeader().getRawData().toBuilder()
            .setTxTrieRoot(MerkleTree.getInstance().createTree(ids).getRoot().getHash()
                .getByteString()).build();

    this.block = this.block.toBuilder().setBlockHeader(
        this.block.getBlockHeader().toBuilder().setRawData(blockHeaderRaw)).build();
  }

  public BlockWrapper(Block block) {
    super(block.getBlockHeader());
    this.block = block;
    initTxs();
  }

  public BlockWrapper(byte[] data) throws BadItemException {
    super();
    try {
      this.block = Block.parseFrom(data);
      this.blockHeader = this.block.getBlockHeader();
      initTxs();
    } catch (InvalidProtocolBufferException e) {
      throw new BadItemException("Block proto data parse exception");
    }
  }

  public byte[] getData() {
    return this.block.toByteArray();

  }

  private StringBuffer toStringBuff = new StringBuffer();

  @Override
  public String toString() {
//    toStringBuff.setLength(0);
//
//    toStringBuff.append("BlockWrapper \n[ ");
//    toStringBuff.append("hash=").append(getBlockId()).append("\n");
//    toStringBuff.append("number=").append(getNum()).append("\n");
//    toStringBuff.append("parentId=").append(getParentHash()).append("\n");
//    toStringBuff.append("witness address=")
//        .append(ByteUtil.toHexString(getWitnessAddress().toByteArray())).append("\n");
//
//    toStringBuff.append("generated by myself=").append(generatedByMyself).append("\n");
//    toStringBuff.append("generate time=").append(Time.getTimeString(getTimeStamp())).append("\n");
//
//    AtomicInteger index = new AtomicInteger();
//    if (!getTransactions().isEmpty()) {
//      toStringBuff.append("merkle root=").append(getMerkleRoot()).append("\n");
//      toStringBuff.append("txs size=").append(getTransactions().size()).append("\n");
//      toStringBuff.append("tx: {");
//      getTransactions().forEach(tx -> toStringBuff
//          .append(index.getAndIncrement()).append(":")
//          .append(tx).append("\n"));
//      toStringBuff.append("}");
//    } else {
//      toStringBuff.append("txs are empty\n");
//    }
//    toStringBuff.append("]");
//    return toStringBuff.toString();
    return "";
  }

}
