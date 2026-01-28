#!/usr/bin/env python3
import http.server
import socketserver
import socket

# Get the local IP address that the emulator can reach
hostname = socket.gethostname()
local_ip = socket.gethostbyname(hostname)

PORT = 8000

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(b"""
        <html>
        <head><title>Test Page</title></head>
        <body>
            <h1>Local Web Server Working!</h1>
            <p>This is a test page to verify browser connectivity.</p>
            <p>Server IP: """ + local_ip.encode() + b"""</p>
        </body>
        </html>
        """)

with socketserver.TCPServer(("", PORT), MyHTTPRequestHandler) as httpd:
    print(f"Server running at http://{local_ip}:{PORT}")
    print(f"Access from emulator at: http://10.0.2.2:{PORT}")
    httpd.serve_forever()
