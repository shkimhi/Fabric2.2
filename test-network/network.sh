#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#

# 이 스크립트는 스마트 계약 및 애플리케이션을 테스트하기 위해 Hyperledger Fabric 네트워크를 불러옵니다. 
# 테스트 네트워크는 각각 피어가 하나씩 있는 두 조직과 단일 노드 Raft 주문 서비스로 구성됩니다.
# 사용자는 이 스크립트를 사용하여 채널을 만들고 채널에 체인코드를 배포할 수도 있습니다
#
# $PWD/../bin을 PATH에 추가하여 올바른 바이너리를 선택했는지 확인합니다.
# 원하는 경우 설치된 도구 버전을 해결하기 위해 주석 처리할 수 있습니다.
export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/configtx
export VERBOSE=false

. scripts/utils.sh

# CONTAINER_IDS 획득 및 제거
# TODO 이것을 선택 사항으로 만들 수 있음 - 다른 컨테이너를 지울 수 있음
# 이 함수는 네트워크를 종료할 때 호출됩니다.
function clearContainers() {
  CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /dev-peer.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == " " ]; then
    infoln "No containers available for deletion"
  else
    docker rm -f $CONTAINER_IDS
  fi
}

# 이 설정의 일부로 생성된 이미지를 삭제합니다. 특히 다음 이미지는 종종 남겨집니다.
# 이 함수는 네트워크를 종료할 때 호출됩니다.
function removeUnwantedImages() {
  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /dev-peer.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    infoln "No images available for deletion"
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi
}

# 테스트 네트워크에서 작동하지 않는 것으로 알려진 패브릭 버전
NONWORKING_VERSIONS="^1\.0\. ^1\.1\. ^1\.2\. ^1\.3\. ^1\.4\."

# 적절한 버전의 패브릭 바이너리/이미지를 사용할 수 있는지 확인하기 위해 몇 가지 기본적인 온전성 검사를 수행합니다. 
# 향후에는 go 또는 기타 항목의 존재 여부에 대한 추가 검사가 추가될 수 있습니다.
function checkPrereqs() {
  ## 피어 바이너리 및 구성 파일을 복제했는지 확인하십시오.
  peer version > /dev/null 2>&1

  if [[ $? -ne 0 || ! -d "../config" ]]; then
    errorln "피어 바이너리 및 구성 파일을 찾을 수 없습니다.."
    errorln
    errorln "Fabric 문서의 지침에 따라 Fabric 바이너리를 설치하십시오: "
    errorln "https://hyperledger-fabric.readthedocs.io/en/latest/install.html"    
    exit 1
  fi
  # 패브릭 도구 컨테이너를 사용하여 샘플 및 바이너리가 도커 이미지와 일치하는지 확인
  LOCAL_VERSION=$(peer version | sed -ne 's/^ Version: //p')
  DOCKER_IMAGE_VERSION=$(docker run --rm hyperledger/fabric-tools:$IMAGETAG peer version | sed -ne 's/^ Version: //p')

  infoln "LOCAL_VERSION=$LOCAL_VERSION"
  infoln "DOCKER_IMAGE_VERSION=$DOCKER_IMAGE_VERSION"

  if [ "$LOCAL_VERSION" != "$DOCKER_IMAGE_VERSION" ]; then
    warnln "로컬 패브릭 바이너리와 도커 이미지가 동기화되지 않았습니다. 이로 인해 문제가 발생할 수 있습니다."
  fi

  for UNSUPPORTED_VERSION in $NONWORKING_VERSIONS; do
    infoln "$LOCAL_VERSION" | grep -q $UNSUPPORTED_VERSION
    if [ $? -eq 0 ]; then
      fatalln "$LOCAL_VERSION 의 로컬 패브릭 바이너리 버전이 테스트 네트워크에서 지원하는 버전과 일치하지 않습니다."
    fi

    infoln "$DOCKER_IMAGE_VERSION" | grep -q $UNSUPPORTED_VERSION
    if [ $? -eq 0 ]; then
      fatalln "$DOCKER_IMAGE_VERSION 의 Fabric Docker 이미지 버전이 테스트 네트워크에서 지원하는 버전과 일치하지 않습니다."
    fi
  done

## fabric-ca 확인
  if [ "$CRYPTO" == "Certificate Authorities" ]; then

    fabric-ca-client version > /dev/null 2>&1
    if [[ $? -ne 0 ]]; then
      errorln "fabric-ca-client 바이너리를 찾을 수 없습니다.."
      errorln
      errorln "Fabric 문서의 지침에 따라 Fabric 바이너리를 설치하십시오:"
      errorln "https://hyperledger-fabric.readthedocs.io/en/latest/install.html"
      exit 1
    fi
    CA_LOCAL_VERSION=$(fabric-ca-client version | sed -ne 's/ Version: //p')
    CA_DOCKER_IMAGE_VERSION=$(docker run --rm hyperledger/fabric-ca:$CA_IMAGETAG fabric-ca-client version | sed -ne 's/ Version: //p' | head -1)
    infoln "CA_LOCAL_VERSION=$CA_LOCAL_VERSION"
    infoln "CA_DOCKER_IMAGE_VERSION=$CA_DOCKER_IMAGE_VERSION"

    if [ "$CA_LOCAL_VERSION" != "$CA_DOCKER_IMAGE_VERSION" ]; then
      warnln "로컬 fabric-ca 바이너리와 도커 이미지가 동기화되지 않았습니다. 이로 인해 문제가 발생할 수 있습니다."
    fi
  fi
}

# 네트워크를 시작하기 전에 각 조직은 네트워크에서 해당 조직을 정의할 암호화 자료를 생성해야 합니다.
# Hyperledger Fabric은 허가된 블록체인이기 때문에 네트워크의 각 노드와 사용자는 인증서와 키를 사용하여 해당 작업에 서명하고 확인해야 합니다.
# 또한 각 사용자는 네트워크의 구성원으로 인식되는 조직에 속해야 합니다.
# Cryptogen 도구 또는 Fabric CA를 사용하여 조직 암호화 자료를 생성할 수 있습니다.

# 기본적으로 샘플 네트워크는 cryptogen을 사용합니다.
# Cryptogen은 Fabric 네트워크에서 사용할 수 있는 인증서와 키를 빠르게 생성할 수 있는 개발 및 테스트용 도구입니다.
# cryptogen 도구는 "organizations/cryptogen" 디렉터리에서 각 조직에 대한 일련의 구성 파일을 사용합니다.
# Cryptogen은 파일을 사용하여 "조직" 디렉터리의 각 조직에 대한 암호화 자료를 생성합니다.

#  Fabric CA가 암호화 자료를 생성할 수도 있습니다. 
# CA는 각 조직에 대해 유효한 신뢰 루트를 생성하기 위해 생성하는 인증서와 키에 서명합니다.
# 이 스크립트는 Docker Compose를 사용하여 각 피어 조직에 대해 하나씩 세 개의 CA를 가져옵니다.
# Fabric CA 서버를 생성하기 위한 구성 파일은 "organizations/fabric-ca" 디렉토리에 있습니다.
# 동일한 디렉터리 내에서 "registerEnroll.sh" 스크립트는 Fabric CA 클라이언트를 사용하여 
# "organizations/ordererOrganizations" 디렉터리에 테스트 네트워크를 만드는 데 필요한 ID, 인증서 및 MSP 폴더를 만듭니다.

# cryptogen 또는 CA를 사용하여 조직 암호화 자료 생성
function createOrgs() {
  if [ -d "organizations/peerOrganizations" ]; then
    rm -Rf organizations/peerOrganizations && rm -Rf organizations/ordererOrganizations
  fi

  # cryptogen 을 사용하여 crypto material 생성 
  if [ "$CRYPTO" == "cryptogen" ]; then
    which cryptogen
    if [ "$?" -ne 0 ]; then
      fatalln "cryptogen 도구를 찾을 수 없습니다. 종료"
    fi
    infoln "cryptogen 도구를 사용하여 인증서 생성"

    infoln "Org1 ID 만들기"

    set -x
    cryptogen generate --config=./organizations/cryptogen/crypto-config-org1.yaml --output="organizations"
    res=$?
    { set +x; } 2>/dev/null
    if [ $res -ne 0 ]; then
      fatalln "인증서를 생성하지 못했습니다..."
    fi

    infoln "Org2 ID 만들기"

    set -x
    cryptogen generate --config=./organizations/cryptogen/crypto-config-org2.yaml --output="organizations"
    res=$?
    { set +x; } 2>/dev/null
    if [ $res -ne 0 ]; then
      fatalln "인증서를 생성하지 못했습니다..."
    fi

    infoln "오더러 조직 ID 만들기"

    set -x
    cryptogen generate --config=./organizations/cryptogen/crypto-config-orderer.yaml --output="organizations"
    res=$?
    { set +x; } 2>/dev/null
    if [ $res -ne 0 ]; then
      fatalln "인증서를 생성하지 못했습니다..."
    fi

  fi

# Fabric CA를 사용하여 crypto material 생성
  if [ "$CRYPTO" == "Certificate Authorities" ]; then
    infoln "Fabric CA를 사용하여 인증서 생성"

    IMAGE_TAG=${CA_IMAGETAG} docker-compose -f $COMPOSE_FILE_CA up -d 2>&1

    . organizations/fabric-ca/registerEnroll.sh

  while :
    do
      if [ ! -f "organizations/fabric-ca/org1/tls-cert.pem" ]; then
        sleep 1
      else
        break
      fi
    done

    infoln "Org1 ID 만들기"

    createOrg1

    infoln "Org2 ID 만들기"

    createOrg2

    infoln "Orderer 조직 ID 만들기"

    createOrderer

  fi

  infoln "Org1 및 Org2용 CCP 파일 생성"
  ./organizations/ccp-generate.sh
}

# 조직 crypto material을 생성했으면 orderer 시스템 채널의 제네시스 블록을 생성해야 합니다.
#이 블록은 주문자 노드를 불러오고 애플리케이션 채널을 생성하는 데 필요합니다.

# configtxgen 도구는 제네시스 블록을 생성하는 데 사용됩니다.
# Configtxgen은 샘플 네트워크에 대한 정의가 포함된 "configtx.yaml" 파일을 사용합니다.
# 제네시스 블록은 파일 하단의 "TwoOrgsOrdererGenesis" 프로필을 사용하여 정의됩니다.
# 이 프로필은 두 개의 피어 조직으로 구성된 샘플 컨소시엄인 "SampleConsortium"을 정의합니다.
# 이 컨소시엄은 네트워크의 구성원으로 인식되는 조직을 정의합니다.
# 피어 및 순서 지정 조직은 파일 맨 위의 "Profiles" 섹션에서 정의됩니다.
# 각 조직 프로필의 일부로 파일은 각 구성원의 MSP 디렉터리 위치를 가리킵니다.
# 이 MSP는 각 조직의 신뢰 루트를 정의하는 채널 MSP를 만드는 데 사용됩니다.
# 본질적으로 채널 MSP는 노드와 사용자가 네트워크 구성원으로 인식되도록 합니다.
# 이 파일은 또한 각 피어 조직에 대한 앵커 피어를 지정합니다.
# 이후 단계에서 이 동일한 파일을 사용하여 채널 생성 트랜잭션 및 앵커 피어 업데이트를 생성합니다.
#
#
# 다음과 같은 경고가 표시되면 무시해도 됩니다.
#
# [bccsp] GetDefault -> WARN 001 Before using BCCSP, please call InitFactories(). Falling back to bootBCCSP.
#
# 중간 인증서에 관한 로그는 무시해도 됩니다. 이 암호화 구현에서는 이를 사용하지 않습니다.


# Generate orderer system channel genesis block.
# 주문자 시스템 채널 제네시스 블록을 생성합니다.
function createConsortium() {
  which configtxgen
  if [ "$?" -ne 0 ]; then
    fatalln "configtxgen 도구를 찾을 수 없음."
  fi

  infoln "Orderer Genesis 블록 생성"

  # Note: For some unknown reason (at least for now) the block file can't be
  # named orderer.genesis.block or the orderer will fail to launch!
  # 참고: 알 수 없는 이유로(적어도 지금은) 블록 파일의 이름을 orderer.genesis.block으로 지정할 수 없거나 orderer가 시작되지 않습니다!
  set -x
  configtxgen -profile TwoOrgsOrdererGenesis -channelID system-channel -outputBlock ./system-genesis-block/genesis.block
  res=$?
  { set +x; } 2>/dev/null
  if [ $res -ne 0 ]; then
    fatalln "orderer 제네시스 블록 생성 실패..."
  fi
}

# 조직 crypto material와 시스템 채널 제네시스 블록을 생성한 후 이제 피어 및 주문 서비스를 불러올 수 있습니다
# 기본적으로 네트워크 생성을 위한 기본 파일은 ``docker`` 폴더의 "docker-compose-test-net.yaml"입니다.
# 이 파일은 이전에 생성된 crypto material 및 제네시스 블록을 가리키는 환경 변수 및 파일 마운트를 정의합니다.

# docker compose를 사용하여 피어 및 주문자 노드를 불러옵니다.
function networkUp() {
  checkPrereqs
  # 존재하지 않는 경우 아티팩트 생성
  if [ ! -d "organizations/peerOrganizations" ]; then
    createOrgs
    createConsortium
  fi

  COMPOSE_FILES="-f ${COMPOSE_FILE_BASE}"

  if [ "${DATABASE}" == "couchdb" ]; then
    COMPOSE_FILES="${COMPOSE_FILES} -f ${COMPOSE_FILE_COUCH}"
  fi

  IMAGE_TAG=$IMAGETAG docker-compose ${COMPOSE_FILES} up -d 2>&1

  docker ps -a
  if [ $? -ne 0 ]; then
    fatalln "네트워크를 시작할 수 없습니다"
  fi
}

# call the script to create the channel, join the peers of org1 and org2,
# and then update the anchor peers for each organization
function createChannel() {
  # Bring up the network if it is not already up.

  if [ ! -d "organizations/peerOrganizations" ]; then
    infoln "Bringing up network"
    networkUp
  fi

  # now run the script that creates a channel. This script uses configtxgen once
  # more to create the channel creation transaction and the anchor peer updates.
  # configtx.yaml is mounted in the cli container, which allows us to use it to
  # create the channel artifacts
  scripts/createChannel.sh $CHANNEL_NAME $CLI_DELAY $MAX_RETRY $VERBOSE
}


## Call the script to deploy a chaincode to the channel
function deployCC() {
  scripts/deployCC.sh $CHANNEL_NAME $CC_NAME $CC_SRC_PATH $CC_SRC_LANGUAGE $CC_VERSION $CC_SEQUENCE $CC_INIT_FCN $CC_END_POLICY $CC_COLL_CONFIG $CLI_DELAY $MAX_RETRY $VERBOSE

  if [ $? -ne 0 ]; then
    fatalln "Deploying chaincode failed"
  fi
}


# Tear down running network
function networkDown() {
  # stop org3 containers also in addition to org1 and org2, in case we were running sample to add org3
  docker-compose -f $COMPOSE_FILE_BASE -f $COMPOSE_FILE_COUCH -f $COMPOSE_FILE_CA down --volumes --remove-orphans
  docker-compose -f $COMPOSE_FILE_COUCH_ORG3 -f $COMPOSE_FILE_ORG3 down --volumes --remove-orphans
  # Don't remove the generated artifacts -- note, the ledgers are always removed
  if [ "$MODE" != "restart" ]; then
    # Bring down the network, deleting the volumes
    #Cleanup the chaincode containers
    clearContainers
    #Cleanup images
    removeUnwantedImages
    # remove orderer block and other channel configuration transactions and certs
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf system-genesis-block/*.block organizations/peerOrganizations organizations/ordererOrganizations'
    ## remove fabric ca artifacts
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org1/msp organizations/fabric-ca/org1/tls-cert.pem organizations/fabric-ca/org1/ca-cert.pem organizations/fabric-ca/org1/IssuerPublicKey organizations/fabric-ca/org1/IssuerRevocationPublicKey organizations/fabric-ca/org1/fabric-ca-server.db'
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org2/msp organizations/fabric-ca/org2/tls-cert.pem organizations/fabric-ca/org2/ca-cert.pem organizations/fabric-ca/org2/IssuerPublicKey organizations/fabric-ca/org2/IssuerRevocationPublicKey organizations/fabric-ca/org2/fabric-ca-server.db'
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/ordererOrg/msp organizations/fabric-ca/ordererOrg/tls-cert.pem organizations/fabric-ca/ordererOrg/ca-cert.pem organizations/fabric-ca/ordererOrg/IssuerPublicKey organizations/fabric-ca/ordererOrg/IssuerRevocationPublicKey organizations/fabric-ca/ordererOrg/fabric-ca-server.db'
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf addOrg3/fabric-ca/org3/msp addOrg3/fabric-ca/org3/tls-cert.pem addOrg3/fabric-ca/org3/ca-cert.pem addOrg3/fabric-ca/org3/IssuerPublicKey addOrg3/fabric-ca/org3/IssuerRevocationPublicKey addOrg3/fabric-ca/org3/fabric-ca-server.db'
    # remove channel and script artifacts
    docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf channel-artifacts log.txt *.tar.gz'
  fi
}

# Obtain the OS and Architecture string that will be used to select the correct
# native binaries for your platform, e.g., darwin-amd64 or linux-amd64
OS_ARCH=$(echo "$(uname -s | tr '[:upper:]' '[:lower:]' | sed 's/mingw64_nt.*/windows/')-$(uname -m | sed 's/x86_64/amd64/g')" | awk '{print tolower($0)}')
# Using crpto vs CA. default is cryptogen
CRYPTO="cryptogen"
# timeout duration - the duration the CLI should wait for a response from
# another container before giving up
MAX_RETRY=5
# default for delay between commands
CLI_DELAY=3
# channel name defaults to "mychannel"
CHANNEL_NAME="mychannel"
# chaincode name defaults to "NA"
CC_NAME="NA"
# chaincode path defaults to "NA"
CC_SRC_PATH="NA"
# endorsement policy defaults to "NA". This would allow chaincodes to use the majority default policy.
CC_END_POLICY="NA"
# collection configuration defaults to "NA"
CC_COLL_CONFIG="NA"
# chaincode init function defaults to "NA"
CC_INIT_FCN="NA"
# use this as the default docker-compose yaml definition
COMPOSE_FILE_BASE=docker/docker-compose-test-net.yaml
# docker-compose.yaml file if you are using couchdb
COMPOSE_FILE_COUCH=docker/docker-compose-couch.yaml
# certificate authorities compose file
COMPOSE_FILE_CA=docker/docker-compose-ca.yaml
# use this as the docker compose couch file for org3
COMPOSE_FILE_COUCH_ORG3=addOrg3/docker/docker-compose-couch-org3.yaml
# use this as the default docker-compose yaml definition for org3
COMPOSE_FILE_ORG3=addOrg3/docker/docker-compose-org3.yaml
#
# chaincode language defaults to "NA"
CC_SRC_LANGUAGE="NA"
# Chaincode version
CC_VERSION="1.0"
# Chaincode definition sequence
CC_SEQUENCE=1
# default image tag
IMAGETAG="latest"
# default ca image tag
CA_IMAGETAG="latest"
# default database
DATABASE="leveldb"

# Parse commandline args

## Parse mode
if [[ $# -lt 1 ]] ; then
  printHelp
  exit 0
else
  MODE=$1
  shift
fi

# parse a createChannel subcommand if used
if [[ $# -ge 1 ]] ; then
  key="$1"
  if [[ "$key" == "createChannel" ]]; then
      export MODE="createChannel"
      shift
  fi
fi

# parse flags

while [[ $# -ge 1 ]] ; do
  key="$1"
  case $key in
  -h )
    printHelp $MODE
    exit 0
    ;;
  -c )
    CHANNEL_NAME="$2"
    shift
    ;;
  -ca )
    CRYPTO="Certificate Authorities"
    ;;
  -r )
    MAX_RETRY="$2"
    shift
    ;;
  -d )
    CLI_DELAY="$2"
    shift
    ;;
  -s )
    DATABASE="$2"
    shift
    ;;
  -ccl )
    CC_SRC_LANGUAGE="$2"
    shift
    ;;
  -ccn )
    CC_NAME="$2"
    shift
    ;;
  -ccv )
    CC_VERSION="$2"
    shift
    ;;
  -ccs )
    CC_SEQUENCE="$2"
    shift
    ;;
  -ccp )
    CC_SRC_PATH="$2"
    shift
    ;;
  -ccep )
    CC_END_POLICY="$2"
    shift
    ;;
  -cccg )
    CC_COLL_CONFIG="$2"
    shift
    ;;
  -cci )
    CC_INIT_FCN="$2"
    shift
    ;;
  -i )
    IMAGETAG="$2"
    shift
    ;;
  -cai )
    CA_IMAGETAG="$2"
    shift
    ;;
  -verbose )
    VERBOSE=true
    shift
    ;;
  * )
    errorln "Unknown flag: $key"
    printHelp
    exit 1
    ;;
  esac
  shift
done

# Are we generating crypto material with this command?
if [ ! -d "organizations/peerOrganizations" ]; then
  CRYPTO_MODE="with crypto from '${CRYPTO}'"
else
  CRYPTO_MODE=""
fi

# Determine mode of operation and printing out what we asked for
if [ "$MODE" == "up" ]; then
  infoln "Starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds and using database '${DATABASE}' ${CRYPTO_MODE}"
elif [ "$MODE" == "createChannel" ]; then
  infoln "Creating channel '${CHANNEL_NAME}'."
  infoln "If network is not up, starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds and using database '${DATABASE} ${CRYPTO_MODE}"
elif [ "$MODE" == "down" ]; then
  infoln "Stopping network"
elif [ "$MODE" == "restart" ]; then
  infoln "Restarting network"
elif [ "$MODE" == "deployCC" ]; then
  infoln "deploying chaincode on channel '${CHANNEL_NAME}'"
else
  printHelp
  exit 1
fi

if [ "${MODE}" == "up" ]; then
  networkUp
elif [ "${MODE}" == "createChannel" ]; then
  createChannel
elif [ "${MODE}" == "deployCC" ]; then
  deployCC
elif [ "${MODE}" == "down" ]; then
  networkDown
else
  printHelp
  exit 1
fi
