#! /bin/bash

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

 echo ============================== peer0.Org1 채널 가입 현황 ==============================
peer channel list

 echo ============================== peer1.org1 채널 가입 현황 ==============================
CORE_PEER_ADDRESS=localhost:8051 peer channel list

 echo ============================== peer1.org1 채널 가입 ==============================
CORE_PEER_ADDRESS=localhost:8051 peer channel join -b channel-artifacts/mychannel.block

 echo ============================== peer1.org1 체인코드 설치 ==============================
CORE_PEER_ADDRESS=localhost:8051 peer lifecycle chaincode install fabcar.tar.gz

 echo ============================== peer1.org1 채널 가입 및 체인코드 설치 완료 ==============================
 echo ========================================================================================================

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051

 echo ============================== peer0.Org2 채널 가입 현황 ==============================
peer channel list

 echo ==============================  peer1.Org2 채널 가입 현황 ==============================
CORE_PEER_ADDRESS=localhost:10051 peer channel list

 echo ============================== peer1.Org2 채널 가입 ==============================
CORE_PEER_ADDRESS=localhost:10051 peer channel join -b channel-artifacts/mychannel.block

 echo ============================== peer1.Org2 체인코드 설치 ==============================
CORE_PEER_ADDRESS=localhost:10051 peer lifecycle chaincode install fabcar.tar.gz

 echo ============================== peer1.Org2 채널 가입 및 체인코드 설치 완료 ==============================


