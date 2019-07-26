
#include <iostream>
#include <nlohmann/json.hpp>
#undef UNICODE

#define WIN32_LEAN_AND_MEAN

#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <stdlib.h>
#include <stdio.h>

// Need to link with Ws2_32.lib
#pragma comment (lib, "Ws2_32.lib")
// #pragma comment (lib, "Mswsock.lib")

#define DEFAULT_PORT "8888"

using json = nlohmann::json;

int __cdecl main(void) {
	WSADATA wsaData;
	int iResult;

	SOCKET ListenSocket = INVALID_SOCKET;
	SOCKET ClientSocket = INVALID_SOCKET;

	struct addrinfo *result = NULL;
	struct addrinfo hints;

	int iSendResult;
	char *recvbuf;
	int recvbuflen = 1;

	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	// Resolve the server address and port
	iResult = getaddrinfo(NULL, DEFAULT_PORT, &hints, &result);
	if (iResult != 0) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return 1;
	}

	// Create a SOCKET for connecting to server
	ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
	if (ListenSocket == INVALID_SOCKET) {
		printf("socket failed with error: %ld\n", WSAGetLastError());
		freeaddrinfo(result);
		WSACleanup();
		return 1;
	}

	// Setup the TCP listening socket
	iResult = bind(ListenSocket, result->ai_addr, (int)result->ai_addrlen);
	if (iResult == SOCKET_ERROR) {
		printf("bind failed with error: %d\n", WSAGetLastError());
		freeaddrinfo(result);
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	freeaddrinfo(result);

	iResult = listen(ListenSocket, SOMAXCONN);
	if (iResult == SOCKET_ERROR) {
		printf("listen failed with error: %d\n", WSAGetLastError());
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	// Accept a client socket
	ClientSocket = accept(ListenSocket, NULL, NULL);
	if (ClientSocket == INVALID_SOCKET) {
		printf("accept failed with error: %d\n", WSAGetLastError());
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}
	std::cout << "Server connected to client on port " << DEFAULT_PORT << '\n';

	// No longer need server socket
	closesocket(ListenSocket);

	// Receive until the peer shuts down the connection
	do {
		std::string stringMessageSize;
		recvbuf = new char[1];
		do {
			iResult = recv(ClientSocket, recvbuf, recvbuflen, 0);
			if (recvbuf[0] != '{') { 
				stringMessageSize += recvbuf[0];
			}
		} while (recvbuf[0] != '{' && iResult !=0);
		
		// Fix call order
		std::cout << "The senders data says it has message will have a size of " << stringMessageSize << '\n';
		recvbuflen = std::atoi(stringMessageSize.c_str());
		if (errno == ERANGE) {
			printf("The string message size value is too big/small (Number is LONG_MAX or LONG_MIN");
			return 0;
		}
		else if (errno == EINVAL) {
			printf("UNABLE TO CONVERT TO A NUMBER");
			return 0;
		}
		recvbuf[0] = '\0';
		delete[] recvbuf;
		recvbuf = new char[recvbuflen];
		iResult = recv(ClientSocket, recvbuf, recvbuflen, 0);
		recvbuflen++;

		
		if (iResult > 0) {

			printf("Bytes received: %d\n", iResult);
			/*	recvbuf[iResult] = '\0';
				printf("Message: %s\n", recvbuf);*/

			std::string receivedString(recvbuf, iResult);
			receivedString.append("\0");
			receivedString.insert(receivedString.begin(), '{');
			std::cout << receivedString << '\n';
			std::string stringMessageSize = "";
			iResult++;

			//JSON conversion

		/*	json j_complete = json::parse(receivedString);
			std::cout << "String to json : \n" << j_complete << "\n\n";*/


			// Echo the buffer back to the sender
			//std::string s = std::to_string(receivedString.length());
			receivedString = std::to_string(iResult) + receivedString;
			char const *pchar = receivedString.c_str();
			std::cout << pchar << "  zzz   " << iResult;


			iSendResult = send(ClientSocket, pchar, iResult + std::to_string(iResult).length(), 0);

			if (iSendResult == SOCKET_ERROR) {
				printf("send failed with error: %d\n", WSAGetLastError());
				closesocket(ClientSocket);
				WSACleanup();
				return 1;
			}
			printf("Bytes sent: %d\n", iSendResult);
		}
		else if (iResult == 0) {
			printf("Connection closing...\n");
		}
		else {
			printf("recv failed with error: %d\n", WSAGetLastError());
			closesocket(ClientSocket);
			WSACleanup();
			return 1;
		}

	} while (iResult > 0);

	// shutdown the connection since we're done
	iResult = shutdown(ClientSocket, SD_SEND);
	if (iResult == SOCKET_ERROR) {
		printf("shutdown failed with error: %d\n", WSAGetLastError());
		closesocket(ClientSocket);
		WSACleanup();
		return 1;
	}

	// cleanup
	closesocket(ClientSocket);
	WSACleanup();

	system("PAUSE");
	return 0;
}