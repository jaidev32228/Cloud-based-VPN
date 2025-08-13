import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table";
import { useEffect, useState } from "react";
import { getAllUsers } from "@/services/api";
interface User {
  id: number;
  email: string;
  role: string;
  subscription?: {
    planType?: string;
  };
}

export default function AdminDashboard() {
  const [users, setUsers] = useState<User[]>([]); // Properly type the users state
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await getAllUsers();
        setUsers(response); 
      } catch (err) {
        setError(`Failed to fetch users. ${err}`);
      }
    };
    fetchUsers();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-r from-gray-900 to-gray-800 text-white p-6">
      <div className="container py-12">
        <h1 className="text-4xl font-extrabold text-center mb-8">
          Admin Dashboard
        </h1>
        {error && (
          <p className="text-red-500 text-center font-medium">{error}</p>
        )}

        <Card className="bg-gray-800 shadow-lg border border-gray-700 rounded-lg">
          <CardHeader>
            <CardTitle className="text-white text-xl">
              User Management
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table className="w-full text-white">
              <TableHeader>
                <TableRow className="bg-gray-700">
                  <TableHead className="px-4 py-3">ID</TableHead>
                  <TableHead className="px-4 py-3">Email</TableHead>
                  <TableHead className="px-4 py-3">Role</TableHead>
                  <TableHead className="px-4 py-3">Subscription</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.length > 0 ? (
                  users.map((user) => (
                    <TableRow
                      key={user.id}
                      className="hover:bg-gray-700 transition-all"
                    >
                      <TableCell className="px-4 py-3">{user.id}</TableCell>
                      <TableCell className="px-4 py-3">{user.email}</TableCell>
                      <TableCell className="px-4 py-3">{user.role}</TableCell>
                      <TableCell className="px-4 py-3">
                        {user.subscription?.planType || "None"}
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell
                      colSpan={4}
                      className="text-center py-4 text-gray-400"
                    >
                      No users found.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}